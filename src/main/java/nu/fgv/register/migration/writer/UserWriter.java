package nu.fgv.register.migration.writer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import nu.fgv.register.migration.MigrationContext;
import nu.fgv.register.migration.model.Spexare;
import org.apache.commons.validator.routines.EmailValidator;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserWriter extends AbstractWriter implements Writer {

    private final Keycloak keycloakAdminClient;
    private final String keycloakClientId;
    @Value("${spexregister.keycloak.realm}")
    private String keycloakRealm;
    @Value("${spexregister.keycloak.pending-approval-group}")
    private String pendingApprovalGroup;

    protected UserWriter(@Qualifier("targetJdbcTemplate") final JdbcTemplate jdbcTemplate,
                         final Keycloak keycloakAdminClient,
                         final String keycloakClientId) {
        super(jdbcTemplate);
        this.keycloakAdminClient = keycloakAdminClient;
        this.keycloakClientId = keycloakClientId;
    }

    @Override
    public void clean() {
        jdbcTemplate.queryForList("SELECT external_id FROM user", String.class).forEach(externalId -> {
            try (final Response response = keycloakAdminClient
                    .realm(keycloakRealm)
                    .users()
                    .delete(externalId)
            ) {
                if (response.getStatus() != HttpStatus.NO_CONTENT.value() && response.getStatus() != HttpStatus.NOT_FOUND.value()) {
                    throw new IllegalStateException("Could not delete user %s in Keycloak".formatted(externalId));
                }
            }
        });

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.execute("TRUNCATE TABLE user");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    @Override
    public void write(final MigrationContext context) {
        final Map<String, RoleRepresentation> roleRepresentations = Map.of(
                "ADMIN", getRoleRepresentationById("ADMIN"),
                "USER", getRoleRepresentationById("USER")
        );
        final ObjectMapper objectMapper = new ObjectMapper();
        final String pendingApprovalGroupId = keycloakAdminClient
                .realm(keycloakRealm)
                .getGroupByPath("/" + pendingApprovalGroup)
                .getId();
        final Map<Long, Spexare> spexareById = context.getSpexare().stream()
                .collect(Collectors.toMap(Spexare::getId, Function.identity()));

        context.getUsers().forEach(t -> {
            final UserRepresentation userRepresentation = new UserRepresentation();
            final CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
            final String state = mapState(t.getState());
            final boolean pending = "pending".equals(t.getState());

            if (EmailValidator.getInstance().isValid(t.getUid())) {
                userRepresentation.setEmail(t.getUid());
            } else {
                userRepresentation.setUsername(t.getUid());
            }
            if (t.getSpexareId() != null && spexareById.containsKey(t.getSpexareId().longValue())) {
                final Spexare spexare = spexareById.get(t.getSpexareId().longValue());

                userRepresentation.setFirstName(spexare.getFirstName());
                userRepresentation.setLastName(spexare.getLastName());
            }
            userRepresentation.setEnabled(pending || "ACTIVE".equals(state));

            try {
                credentialRepresentation.setCredentialData(objectMapper.writeValueAsString(Map.of(
                        "algorithm", "legacy-fgv",
                        "hashIterations", 20
                )));
                credentialRepresentation.setSecretData(objectMapper.writeValueAsString(Map.of(
                        "value", t.getPassword(),
                        "salt", Base64.getEncoder().encode(t.getPasswordSalt().getBytes(StandardCharsets.US_ASCII))
                )));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
            credentialRepresentation.setTemporary(false);
            userRepresentation.setCredentials(List.of(credentialRepresentation));

            try (final Response response = keycloakAdminClient
                    .realm(keycloakRealm)
                    .users()
                    .create(userRepresentation)
            ) {
                if (response.getStatus() == HttpStatus.CREATED.value()) {
                    final String locationPath = response.getLocation().getPath();
                    final String externalId = locationPath.substring(locationPath.lastIndexOf('/') + 1);

                    try {
                        final UserResource userResource = keycloakAdminClient
                                .realm(keycloakRealm)
                                .users()
                                .get(externalId);

                        if (pending) {
                            userResource.joinGroup(pendingApprovalGroupId);
                        } else {
                            final List<RoleRepresentation> authorities = t.getGroups().stream()
                                    .map(this::mapGroup)
                                    .map(roleRepresentations::get)
                                    .toList();

                            userResource
                                    .roles()
                                    .clientLevel(keycloakClientId)
                                    .add(authorities);
                        }

                        jdbcTemplate.execute(String.format("""
                                        INSERT INTO user
                                        (id, external_id, state_id, created_by, created_at, last_modified_by, last_modified_at, version)
                                        values
                                        (%s, '%s', '%s', '%s', '%s', '%s', '%s', 0)""",
                                t.getId(), externalId, state,
                                mapUser(context.getUsers(), t.getCreatedBy()), formatTimestamp(t.getCreatedAt()), mapUser(context.getUsers(), t.getLastModifiedBy()), formatTimestamp(t.getLastModifiedAt())));
                    } catch (final Exception e) {
                        keycloakAdminClient.realm(keycloakRealm).users().delete(externalId).close();
                        throw new IllegalStateException("Could not retrieve newly created user %s in Keycloak".formatted(t.getUid()), e);
                    }
                } else {
                    throw new IllegalStateException("Could not create user %s in Keycloak".formatted(t.getUid()));
                }
            }
        });

        // Spexare
        context.getUsers().stream().filter(t -> t.getSpexareId() != null).forEach(t ->
                jdbcTemplate.execute(String.format("""
                                UPDATE user
                                SET spexare_id = %s
                                WHERE id = %s""",
                        t.getSpexareId(), t.getId()))
        );
    }

    private String mapGroup(final String group) {
        switch (group) {
            case "Administrators" -> {
                return "ADMIN";
            }
            case "Users" -> {
                return "USER";
            }
            default -> throw new IllegalArgumentException("Unknown group");
        }
    }

    private String mapState(final String state) {
        switch (state) {
            case "active" -> {
                return "ACTIVE";
            }
            case "inactive", "pending" -> {
                return "INACTIVE";
            }
            default -> throw new IllegalArgumentException("Unknown state");
        }
    }

    private RoleRepresentation getRoleRepresentationById(final String id) {
        final List<RoleRepresentation> roles = keycloakAdminClient.realm(keycloakRealm).clients().get(keycloakClientId).roles().list();

        return roles.stream()
                .filter(r -> r.getName().equals(id))
                .findFirst()
                .orElseGet(RoleRepresentation::new); // Should never happen
    }

}

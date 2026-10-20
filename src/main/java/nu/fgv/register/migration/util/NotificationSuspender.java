package nu.fgv.register.migration.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RealmEventsConfigRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.stream.StreamSupport;

@Slf4j
@Service
public class NotificationSuspender {

    static final String REQUIRED_ROLE = "manage-events";
    private static final String REALM_MANAGEMENT = "realm-management";

    private final Keycloak keycloakAdminClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${spexregister.keycloak.realm}")
    private String keycloakRealm;
    @Value("${spexregister.keycloak.admin.client-id}")
    private String keycloakAdminClientId;
    @Value("${spexregister.keycloak.notification-listener}")
    private String notificationListener;

    public NotificationSuspender(final Keycloak keycloakAdminClient) {
        this.keycloakAdminClient = keycloakAdminClient;
    }

    public void requireRole() {
        if (!grantedRoles().contains(REQUIRED_ROLE)) {
            throw new IllegalStateException("""
                    The Keycloak client %1$s lacks the %2$s role %3$s, which the migration needs to turn off the \
                    event listener %4$s so that migrated users aren't emailed. In the admin console of realm %5$s, \
                    give the client's service account the role (Clients -> %1$s -> Service accounts roles) and add it \
                    to the client's role scope (Client scopes -> %1$s-dedicated -> Scope), then run again. Remove both \
                    after the migration.""".formatted(keycloakAdminClientId, REALM_MANAGEMENT, REQUIRED_ROLE, notificationListener, keycloakRealm));
        }
    }

    public Suspension suspend() {
        final RealmResource realm = keycloakAdminClient.realm(keycloakRealm);
        final RealmEventsConfigRepresentation config = realm.getRealmEventsConfig();
        final List<String> listeners = List.copyOf(config.getEventsListeners());

        if (!listeners.contains(notificationListener)) {
            log.info("Event listener {} isn't active, so no emails will be sent", notificationListener);
            return () -> {
            };
        }

        config.setEventsListeners(listeners.stream().filter(listener -> !listener.equals(notificationListener)).toList());
        realm.updateRealmEventsConfig(config);
        log.info("Turned off event listener {}", notificationListener);

        return () -> {
            try {
                final RealmEventsConfigRepresentation current = realm.getRealmEventsConfig();

                current.setEventsListeners(listeners);
                realm.updateRealmEventsConfig(current);
                log.info("Turned event listener {} back on", notificationListener);
            } catch (final RuntimeException e) {
                log.error("Could not turn event listener {} back on. Add it again under Realm settings -> Events -> Event listeners in realm {}",
                        notificationListener, keycloakRealm, e);
            }
            log.warn("Remove the {} role {} from client {} again, both from its service account and from its role scope",
                    REALM_MANAGEMENT, REQUIRED_ROLE, keycloakAdminClientId);
        };
    }

    private List<String> grantedRoles() {
        final String token = keycloakAdminClient.tokenManager().grantToken().getToken();

        try {
            final JsonNode claims = objectMapper.readTree(Base64.getUrlDecoder().decode(token.split("\\.")[1]));
            final JsonNode roles = claims.path("resource_access").path(REALM_MANAGEMENT).path("roles");

            return StreamSupport.stream(roles.spliterator(), false).map(JsonNode::asText).toList();
        } catch (final IOException e) {
            throw new IllegalStateException("Could not read the roles of the Keycloak client's access token", e);
        }
    }

    public interface Suspension extends AutoCloseable {

        @Override
        void close();
    }
}

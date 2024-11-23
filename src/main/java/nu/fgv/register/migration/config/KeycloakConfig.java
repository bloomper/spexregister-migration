package nu.fgv.register.migration.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.ClientRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {

    private final String keycloakUrl;
    private final String keycloakRealm;
    private final String keycloakAdminClientId;
    private final String keycloakAdminClientSecret;
    private final String keycloakClientClientId;

    public KeycloakConfig(@Value("${spexregister.keycloak.url}") final String keycloakUrl,
                          @Value("${spexregister.keycloak.realm}") final String keycloakRealm,
                          @Value("${spexregister.keycloak.admin.client-id}") final String keycloakAdminClientId,
                          @Value("${spexregister.keycloak.admin.client-secret}") final String keycloakAdminClientSecret,
                          @Value("${spexregister.keycloak.client.client-id}") final String keycloakClientClientId) {
        this.keycloakUrl = keycloakUrl;
        this.keycloakRealm = keycloakRealm;
        this.keycloakAdminClientId = keycloakAdminClientId;
        this.keycloakAdminClientSecret = keycloakAdminClientSecret;
        this.keycloakClientClientId = keycloakClientClientId;
    }

    @Bean
    public Keycloak keycloakAdminClient() {
        return KeycloakBuilder.builder()
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .serverUrl(keycloakUrl)
                .realm(keycloakRealm)
                .clientId(keycloakAdminClientId)
                .clientSecret(keycloakAdminClientSecret)
                .build();
    }

    @Bean
    public String keycloakClientId(final Keycloak keycloakAdminClient) {
        return keycloakAdminClient
                .realm(keycloakRealm)
                .clients()
                .findByClientId(keycloakClientClientId)
                .stream()
                .filter(c -> c.getClientId().equals(keycloakClientClientId))
                .findFirst()
                .map(ClientRepresentation::getId)
                .orElseThrow(() -> new RuntimeException("Could not retrieve id of client in Keycloak"));
    }
}

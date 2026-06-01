package com.example.foodndeliv.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Based on https://github.com/phuongtailtranminh/Keycloak-Admin-Client-Spring-Boot-Demo/blob/master/src/main/java/me/phuongtm/KeycloakService.java
// + https://gist.github.com/thomasdarimont/c4e739c5a319cf78a4cff3b87173a84b
//+https://www.keycloak.org/docs-api/latest/javadocs/org/keycloak/admin/client/package-summary.html

@Configuration
public class KeycloakConfig {

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.username}")
    private String username;

    @Value("${keycloak.password}")
    private String password;

    @Bean
    public Keycloak keycloakClient() {


        //See: https://www.keycloak.org/docs/latest/server_development/index.html#admin-rest-api
        // + https://gist.github.com/thomasdarimont/c4e739c5a319cf78a4cff3b87173a84b

        //Test with curl -d "client_id=admin-cli" -d "client_secret=<secret>" -d "grant_type=client_credentials" "http://my-keycloak:80/realms/master/protocol/openid-connect/token"

        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
    }
}


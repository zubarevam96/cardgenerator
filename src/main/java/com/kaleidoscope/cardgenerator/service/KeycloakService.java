package com.kaleidoscope.cardgenerator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
@Service
public class KeycloakService {
    private final static Logger logger = LoggerFactory.getLogger(KeycloakService.class);

    @Value("${keycloak.realm}")
    private String keycloakRealm;

    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    public void createUser(String username, String password) throws HttpClientErrorException {
        String adminToken = getAdminToken();
        String url = keycloakServerUrl + "/admin/realms/" + keycloakRealm + "/users";
        Map<String, Object> body = Map.of(
                "username", username,
                "enabled", true,
                "credentials", List.of(Map.of(
                        "type", "password",
                        "value", password,
                        "temporary", false
                ))
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        //noinspection rawtypes
        ResponseEntity<Map> response = new RestTemplate().postForEntity(
                url, new HttpEntity<>(body, headers), Map.class);
        logger.info("Registered new user '{}' in keycloak", username);
    }

    private String getAdminToken() {
        String url = keycloakServerUrl + "/realms/" + keycloakRealm + "/protocol/openid-connect/token";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        var request = new HttpEntity<>(body, headers);

        //noinspection rawtypes
        ResponseEntity<Map> response = new RestTemplate().postForEntity(url, request, Map.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new AuthorizationDeniedException("Error while getting admin token - " +
                    "received response with status code " + response.getStatusCode() +
                    " and body " + response.getBody());
        }

        //noinspection DataFlowIssue
        return (String) response.getBody().get("access_token");
    }

    public String getToken(String username, String password) {
        String url = keycloakServerUrl + "/realms/" + keycloakRealm + "/protocol/openid-connect/token";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("username", username);
        body.add("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        var request = new HttpEntity<>(body, headers);

        try {
            //noinspection rawtypes
            ResponseEntity<Map> response = new RestTemplate().postForEntity(url, request, Map.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new AuthorizationDeniedException("Error while getting user token - " +
                        "received response with status code " + response.getStatusCode() +
                        " and body " + response.getBody());
            }

            //noinspection DataFlowIssue
            return (String) response.getBody().get("access_token");
        } catch (HttpClientErrorException e) {
            logger.error("Failed to get token for user {}: {}", username, e.getMessage());
            throw new AuthorizationDeniedException("Invalid username or password");
        }
    }
}

package ua.nure.beautysalon.web.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

@Service
@Slf4j
public class UserServiceClient {

    private final RestTemplate restTemplate;
    private final String userServiceUrl;

    public UserServiceClient(RestTemplate restTemplate, 
                           @Value("${user-service.url:http://user-service:8081}") String userServiceUrl) {
        this.restTemplate = restTemplate;
        this.userServiceUrl = userServiceUrl;
    }

    public boolean validateToken(String token) {
        try {
            String url = userServiceUrl + "/api/auth/validate?token=" + token;
            ResponseEntity<Boolean> response = restTemplate.getForEntity(url, Boolean.class);
            Boolean isValid = response.getBody();
            log.debug("Token validation result: {}", isValid);
            return Boolean.TRUE.equals(isValid);
        } catch (RestClientException e) {
            log.warn("Failed to validate token: {}", e.getMessage());
            return false;
        }
    }

    public String getUserFromToken(String token) {
        try {
            String url = userServiceUrl + "/api/auth/user?token=" + token;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String email = response.getBody();
            log.debug("Extracted email from token: {}", email);
            return email;
        } catch (RestClientException e) {
            log.warn("Failed to extract user from token: {}", e.getMessage());
            return null;
        }
    }
}
package ua.nure.beautysalon.web.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ua.nure.beautysalon.web.dto.LoginRequest;
import ua.nure.beautysalon.web.dto.LoginResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final RestTemplate restTemplate;
    
    @Value("${user-service.url:http://user-service:8081}")
    private String userServiceUrl;

    public LoginResponse login(LoginRequest loginRequest) {
        try {
            String url = userServiceUrl + "/api/auth/login";
            
            log.debug("Attempting login for user: {}", loginRequest.getEmail());
            
            ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                url, loginRequest, LoginResponse.class);
            
            LoginResponse loginResponse = response.getBody();
            
            if (loginResponse != null) {
                log.info("Login successful for user: {} with role: {}", 
                    loginResponse.getEmail(), loginResponse.getRole());
                return loginResponse;
            } else {
                throw new RuntimeException("Empty response from user service");
            }
            
        } catch (Exception e) {
            log.error("Login failed for user: {} - {}", loginRequest.getEmail(), e.getMessage());
            throw new RuntimeException("Login failed: " + e.getMessage());
        }
    }
}
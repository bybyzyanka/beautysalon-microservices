package ua.nure.beautysalon.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.nure.beautysalon.web.dto.UserDTO;
import ua.nure.beautysalon.web.feign.UserServiceClient;

@Slf4j
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final UserServiceClient userServiceClient;

    @GetMapping("/user/{email}")
    public String testUserService(@PathVariable String email) {
        try {
            log.info("Testing connection to user service for email: {}", email);
            UserDTO user = userServiceClient.findByEmail(email);
            log.info("Successfully retrieved user: {}", user.getEmail());
            return "Success: Found user " + user.getEmail() + " with role " + user.getRole();
        } catch (Exception e) {
            log.error("Failed to connect to user service: {}", e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/admin")
    public String testAdminUser() {
        return testUserService("admin@beautysalon.com");
    }
}
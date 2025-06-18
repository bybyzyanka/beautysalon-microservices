package ua.nure.beautysalon.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.nure.beautysalon.web.dto.UserDTO;
import ua.nure.beautysalon.web.feign.UserServiceClient;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Date;

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

    @GetMapping("/auth")
    public String testAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("Current authentication: {}", auth);

        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return "Authenticated as: " + auth.getName() + " with authorities: " + auth.getAuthorities();
        } else {
            return "Not authenticated - auth object: " + auth;
        }
    }

    @GetMapping("/session")
    public String testSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        StringBuilder sb = new StringBuilder();

        sb.append("Session ID: ").append(session != null ? session.getId() : "No session").append("<br>");
        sb.append("Session creation time: ").append(session != null ? new Date(session.getCreationTime()) : "N/A").append("<br>");
        sb.append("Session last accessed: ").append(session != null ? new Date(session.getLastAccessedTime()) : "N/A").append("<br>");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        sb.append("Authentication: ").append(auth != null ? auth.getName() : "null").append("<br>");
        sb.append("Is authenticated: ").append(auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")).append("<br>");

        return sb.toString();
    }

    @GetMapping("/login-test")
    public String testLogin() {
        try {
            UserDTO user = userServiceClient.findByEmail("admin@beautysalon.com");
            String passwordInfo = user.getPassword() != null ?
                    "Password present: true, starts with $2a: " + user.getPassword().startsWith("$2a$") +
                            ", length: " + user.getPassword().length() :
                    "Password is null";

            return "User service connection OK. User: " + user.getEmail() +
                    ", Role: " + user.getRole() + ", " + passwordInfo;
        } catch (Exception e) {
            return "User service connection failed: " + e.getMessage();
        }
    }
}
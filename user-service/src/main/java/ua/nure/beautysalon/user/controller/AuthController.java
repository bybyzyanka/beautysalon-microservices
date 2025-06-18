package ua.nure.beautysalon.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.nure.beautysalon.user.dto.LoginRequest;
import ua.nure.beautysalon.user.dto.LoginResponse;
import ua.nure.beautysalon.user.dto.UserDTO;
import ua.nure.beautysalon.user.entity.User;
import ua.nure.beautysalon.user.service.UserService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse response = userService.login(loginRequest);
        System.out.println("Login response: " + response.getEmail() + " " + response.getToken() + " " + response.getToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup")
    @Operation(summary = "User registration", description = "Register a new user")
    public ResponseEntity<UserDTO> signup(@RequestParam String email,
                                          @RequestParam String password,
                                          @RequestParam String role) {
        UserDTO user = userService.signup(email, password, role);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate token", description = "Validate JWT token")
    public ResponseEntity<Boolean> validateToken(@RequestParam String token) {
        boolean isValid = userService.validateToken(token);
        return ResponseEntity.ok(isValid);
    }

    @GetMapping("/user")
    @Operation(summary = "Get user by token", description = "Extract user info from JWT token")
    public ResponseEntity<String> getUserFromToken(@RequestParam String token) {
        if (userService.validateToken(token)) {
            String email = userService.extractEmailFromToken(token);
            return ResponseEntity.ok(email);
        }
        return ResponseEntity.badRequest().body("Invalid token");
    }

    // This endpoint should be accessible without authentication for UserDetailsService
    @GetMapping("/user/{email}")
    @Operation(summary = "Get user by email", description = "Get user details by email (internal use)")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        try {
            User user = userService.findByEmail(email);
            UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getId());
            userDTO.setEmail(user.getEmail());
            userDTO.setRole(user.getRole());
            userDTO.setPassword(user.getPassword()); // For authentication
            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/password")
    @Operation(summary = "Update password", description = "Update user password")
    public ResponseEntity<Void> updatePassword(@RequestParam String email,
                                               @RequestParam String password) {
        try {
            userService.updatePasswordByEmail(email, password);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
package ua.nure.beautysalon.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.nure.beautysalon.user.dto.LoginRequest;
import ua.nure.beautysalon.user.dto.LoginResponse;
import ua.nure.beautysalon.user.dto.UserDTO;
import ua.nure.beautysalon.user.entity.User;
import ua.nure.beautysalon.user.repository.UserRepository;
import ua.nure.beautysalon.user.service.UserService;
import ua.nure.beautysalon.user.util.JwtUtil;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public UserDTO signup(String email, String password, String role) {
        logger.info("Attempting to create user with email: {} and role: {}", email, role);

        if (userRepository.existsByEmail(email)) {
            logger.warn("User already exists with email: {}", email);
            throw new IllegalArgumentException("User already exists with email: " + email);
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        User savedUser = userRepository.save(user);
        logger.info("User created successfully with email: {} and ID: {}", savedUser.getEmail(), savedUser.getId());

        return new UserDTO(savedUser.getId(), savedUser.getEmail(), savedUser.getRole(), null);
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        logger.info("Attempting login for email: {}", loginRequest.getEmail());

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> {
                    logger.warn("Login failed - user not found: {}", loginRequest.getEmail());
                    return new IllegalArgumentException("Invalid credentials");
                });

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            logger.warn("Login failed - invalid password for email: {}", loginRequest.getEmail());
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        logger.info("Login successful for email: {} with role: {}", user.getEmail(), user.getRole());

        return new LoginResponse(token, user.getRole(), user.getEmail());
    }

    @Override
    public User findByEmail(String email) {
        logger.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("User not found with email: {}", email);
                    return new IllegalArgumentException("User not found with email: " + email);
                });
    }

    @Override
    public void deleteUserById(Long id) {
        logger.info("Deleting user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Cannot delete - user not found with ID: {}", id);
                    return new IllegalArgumentException("User not found with ID: " + id);
                });
        userRepository.delete(user);
        logger.info("User deleted successfully with ID: {}", id);
    }

    @Override
    public void updatePasswordByEmail(String email, String newPassword) {
        logger.info("Updating password for email: {}", email);
        User user = findByEmail(email);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.info("Password updated successfully for email: {}", email);
    }

    @Override
    public boolean validateToken(String token) {
        boolean isValid = jwtUtil.validateToken(token);
        logger.debug("Token validation result: {}", isValid);
        return isValid;
    }

    @Override
    public String extractEmailFromToken(String token) {
        String email = jwtUtil.extractEmail(token);
        logger.debug("Extracted email from token: {}", email);
        return email;
    }
}
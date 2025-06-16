package ua.nure.beautysalon.user.service.impl;

import lombok.RequiredArgsConstructor;
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

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public UserDTO signup(String email, String password, String role) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User already exists");
        }
        
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        User savedUser = userRepository.save(user);
        return new UserDTO(savedUser.getId(), savedUser.getEmail(), savedUser.getRole(), null);
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        return new LoginResponse(token, user.getRole(), user.getEmail());
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Override
    public void deleteUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        userRepository.delete(user);
    }

    @Override
    public void updatePasswordByEmail(String email, String newPassword) {
        User user = findByEmail(email);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    @Override
    public String extractEmailFromToken(String token) {
        return jwtUtil.extractEmail(token);
    }
}
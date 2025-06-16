package ua.nure.beautysalon.user.service;

import ua.nure.beautysalon.user.dto.LoginRequest;
import ua.nure.beautysalon.user.dto.LoginResponse;
import ua.nure.beautysalon.user.dto.UserDTO;
import ua.nure.beautysalon.user.entity.User;

public interface UserService {
    UserDTO signup(String email, String password, String role);
    LoginResponse login(LoginRequest loginRequest);
    User findByEmail(String email);
    void deleteUserById(Long id);
    void updatePasswordByEmail(String email, String password);
    boolean validateToken(String token);
    String extractEmailFromToken(String token);
}
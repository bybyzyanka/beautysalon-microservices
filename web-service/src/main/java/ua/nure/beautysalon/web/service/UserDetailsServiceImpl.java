package ua.nure.beautysalon.web.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ua.nure.beautysalon.web.dto.UserDTO;
import ua.nure.beautysalon.web.feign.UserServiceClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserServiceClient userServiceClient;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("=== UserDetailsService.loadUserByUsername called for: {}", username);

        try {
            // Call the user service to get user details
            UserDTO userDto = userServiceClient.findByEmail(username);
            log.info("Successfully retrieved user from service: {} with role: {}", userDto.getEmail(), userDto.getRole());

            // Debug: Log password info (without revealing the actual password)
            String password = userDto.getPassword();
            log.info("Password present: {}, starts with $2a: {}, length: {}",
                    password != null,
                    password != null && password.startsWith("$2a$"),
                    password != null ? password.length() : 0);

            // Create Spring Security UserDetails object
            UserDetails userDetails = User.builder()
                    .username(userDto.getEmail())
                    .password(userDto.getPassword()) // This should be the encoded password from the database
                    .roles(userDto.getRole()) // Spring Security will add ROLE_ prefix automatically
                    .disabled(false)
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .build();

            log.info("=== Created UserDetails for user: {} with authorities: {}",
                    userDetails.getUsername(), userDetails.getAuthorities());
            log.info("=== UserDetails password present: {}", userDetails.getPassword() != null);

            return userDetails;

        } catch (Exception e) {
            log.error("=== Failed to load user details for username: {} - Error: {}", username, e.getMessage(), e);
            throw new UsernameNotFoundException("User not found: " + username, e);
        }
    }
}
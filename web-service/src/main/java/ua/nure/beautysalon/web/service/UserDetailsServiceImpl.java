package ua.nure.beautysalon.web.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ua.nure.beautysalon.web.feign.UserServiceClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserServiceClient userServiceClient;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Attempting to load user: {}", username);

        try {
            var userDto = userServiceClient.findByEmail(username);
            log.info("Successfully found user: {} with role: {}", userDto.getEmail(), userDto.getRole());

            UserDetails user = User.builder()
                    .username(userDto.getEmail())
                    .password(userDto.getPassword())
                    .roles(userDto.getRole())
                    .build();

            log.info("Created UserDetails for: {}", userDto.getEmail());
            return user;

        } catch (Exception e) {
            log.error("Failed to load user: {} - Error: {}", username, e.getMessage(), e);
            throw new UsernameNotFoundException("User not found: " + username, e);
        }
    }
}
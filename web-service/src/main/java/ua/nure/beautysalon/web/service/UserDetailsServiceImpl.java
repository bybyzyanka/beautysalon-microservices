package ua.nure.beautysalon.web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ua.nure.beautysalon.web.feign.UserServiceClient;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserServiceClient userServiceClient;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            var userDto = userServiceClient.findByEmail(username);
            return User.builder()
                    .username(userDto.getEmail())
                    .password(userDto.getPassword())
                    .roles(userDto.getRole())
                    .build();
        } catch (Exception e) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
    }
}
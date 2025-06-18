package ua.nure.beautysalon.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Allow access to authentication endpoints
                        .requestMatchers("/api/auth/login", "/api/auth/signup", "/api/auth/validate", "/api/auth/user/**", "/api/auth/password").permitAll()
                        // Allow access to H2 console for development
                        .requestMatchers("/h2-console/**").permitAll()
                        // Allow access to actuator endpoints
                        .requestMatchers("/actuator/**").permitAll()
                        // Require authentication for all other requests
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers
                        // Allow H2 console to work in frames
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                )
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable());

        return http.build();
    }
}
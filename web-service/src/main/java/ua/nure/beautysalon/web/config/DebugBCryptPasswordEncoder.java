package ua.nure.beautysalon.web.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Slf4j
public class DebugBCryptPasswordEncoder extends BCryptPasswordEncoder {

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        log.info("=== Password matching ===");
        log.info("Raw password length: {}", rawPassword != null ? rawPassword.length() : 0);
        log.info("Encoded password present: {}", encodedPassword != null);
        log.info("Encoded password starts with $2a: {}", encodedPassword != null && encodedPassword.startsWith("$2a$"));
        
        boolean matches = super.matches(rawPassword, encodedPassword);
        log.info("Password match result: {}", matches);
        log.info("=== End password matching ===");
        
        return matches;
    }
}
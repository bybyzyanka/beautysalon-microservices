package ua.nure.beautysalon.web.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
public class DebugDaoAuthenticationProvider extends DaoAuthenticationProvider {

    public DebugDaoAuthenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        setUserDetailsService(userDetailsService);
        setPasswordEncoder(passwordEncoder);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.info("=== DaoAuthenticationProvider.authenticate called ===");
        log.info("Authentication name: {}", authentication.getName());
        log.info("Authentication credentials present: {}", authentication.getCredentials() != null);

        try {
            Authentication result = super.authenticate(authentication);
            log.info("=== Authentication SUCCESS ===");
            log.info("Authenticated user: {}", result.getName());
            log.info("Authorities: {}", result.getAuthorities());
            return result;
        } catch (AuthenticationException e) {
            log.error("=== Authentication FAILED ===");
            log.error("Exception: {}", e.getClass().getSimpleName());
            log.error("Message: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {
        log.info("=== additionalAuthenticationChecks called ===");
        log.info("UserDetails username: {}", userDetails.getUsername());
        log.info("UserDetails authorities: {}", userDetails.getAuthorities());
        log.info("UserDetails enabled: {}", userDetails.isEnabled());
        log.info("UserDetails account non-expired: {}", userDetails.isAccountNonExpired());
        log.info("UserDetails account non-locked: {}", userDetails.isAccountNonLocked());
        log.info("UserDetails credentials non-expired: {}", userDetails.isCredentialsNonExpired());
        log.info("Authentication credentials present: {}", authentication.getCredentials() != null);

        try {
            super.additionalAuthenticationChecks(userDetails, authentication);
            log.info("=== Password check PASSED ===");
        } catch (BadCredentialsException e) {
            log.error("=== Password check FAILED ===");
            log.error("Exception: {}", e.getMessage());
            throw e;
        }
    }
}
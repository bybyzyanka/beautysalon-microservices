package ua.nure.beautysalon.web.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    public CustomAuthenticationSuccessHandler() {
        setDefaultTargetUrl("/");
        setAlwaysUseDefaultTargetUrl(true);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {
        
        log.info("=== Authentication successful! ===");
        log.info("Authenticated user: {}", authentication.getName());
        log.info("Authorities: {}", authentication.getAuthorities());
        log.info("Authentication class: {}", authentication.getClass().getName());
        
        HttpSession session = request.getSession();
        log.info("Session ID: {}", session.getId());
        log.info("Session creation time: {}", session.getCreationTime());
        
        // Call parent method to handle redirect
        super.onAuthenticationSuccess(request, response, authentication);
        
        log.info("=== Redirect completed ===");
    }
}
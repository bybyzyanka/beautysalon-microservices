package ua.nure.beautysalon.web.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, 
                        AuthenticationException authException) throws IOException, ServletException {
        
        log.debug("Authentication required for path: {}", request.getRequestURI());
        
        // Check if it's an AJAX request
        String requestedWith = request.getHeader("X-Requested-With");
        String acceptHeader = request.getHeader("Accept");
        
        if ("XMLHttpRequest".equals(requestedWith) || 
            (acceptHeader != null && acceptHeader.contains("application/json"))) {
            // For AJAX requests, return 401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Authentication required\"}");
        } else {
            // For regular requests, redirect to login
            String redirectUrl = "/login";
            
            // Add the original URL as a parameter so we can redirect back after login
            String requestURI = request.getRequestURI();
            if (requestURI != null && !requestURI.equals("/") && !requestURI.equals("/login")) {
                redirectUrl += "?redirect=" + requestURI;
            }
            
            response.sendRedirect(redirectUrl);
        }
    }
}
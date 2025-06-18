package ua.nure.beautysalon.gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final RestTemplate restTemplate;
    private final String userServiceUrl;

    public JwtAuthenticationFilter(RestTemplate restTemplate,
                                 @Value("${user-service.url:http://user-service:8081}") String userServiceUrl) {
        this.restTemplate = restTemplate;
        this.userServiceUrl = userServiceUrl;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Skip authentication for public endpoints
        if (isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }

        // Check if this is an API call that needs authentication
        if (path.startsWith("/api/") && !path.startsWith("/api/auth/")) {
            return validateAndForwardRequest(exchange, chain);
        }

        // For web pages, let the web service handle authentication
        return chain.filter(exchange);
    }

    private Mono<Void> validateAndForwardRequest(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // Extract JWT token from Authorization header or Cookie
        String token = extractToken(request);

        if (token == null) {
            return unauthorizedResponse(response);
        }

        try {
            // Validate token synchronously (in a real implementation, you'd want to make this reactive)
            boolean isValid = validateToken(token);
            
            if (!isValid) {
                return unauthorizedResponse(response);
            }

            // Token is valid, forward the request with cleaned headers
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Token", token)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            return unauthorizedResponse(response);
        }
    }

    private String extractToken(ServerHttpRequest request) {
        // Try Authorization header first
        List<String> authHeaders = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String authHeader = authHeaders.get(0);
            if (authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
        }

        // Try Cookie
        List<String> cookies = request.getHeaders().get(HttpHeaders.COOKIE);
        if (cookies != null) {
            for (String cookie : cookies) {
                if (cookie.contains("JWT_TOKEN=")) {
                    String[] parts = cookie.split("JWT_TOKEN=");
                    if (parts.length > 1) {
                        String tokenPart = parts[1].split(";")[0];
                        return tokenPart;
                    }
                }
            }
        }

        return null;
    }

    private boolean validateToken(String token) {
        try {
            String url = userServiceUrl + "/api/auth/validate?token=" + token;
            Boolean result = restTemplate.getForObject(url, Boolean.class);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            return false;
        }
    }

    private Mono<Void> unauthorizedResponse(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    private boolean isPublicEndpoint(String path) {
        return path.equals("/login") ||
               path.startsWith("/css/") ||
               path.startsWith("/js/") ||
               path.startsWith("/images/") ||
               path.equals("/favicon.ico") ||
               path.startsWith("/api/auth/") ||
               path.startsWith("/test/") ||
               path.startsWith("/h2-console/");
    }

    @Override
    public int getOrder() {
        return -100; // Execute before routing
    }
}
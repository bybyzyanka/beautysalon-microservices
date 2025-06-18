package ua.nure.beautysalon.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RedirectRewriteFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            ServerHttpRequest request = exchange.getRequest();
            
            // Only process redirect responses
            if (response.getStatusCode() != null && 
                (response.getStatusCode() == HttpStatus.FOUND || 
                 response.getStatusCode() == HttpStatus.MOVED_PERMANENTLY ||
                 response.getStatusCode() == HttpStatus.SEE_OTHER)) {
                
                String location = response.getHeaders().getFirst(HttpHeaders.LOCATION);
                if (location != null) {
                    // Get the original host and scheme from the request
                    String originalScheme = request.getHeaders().getFirst("X-Forwarded-Proto");
                    if (originalScheme == null) {
                        originalScheme = request.getURI().getScheme();
                    }
                    
                    String originalHost = request.getHeaders().getFirst("X-Forwarded-Host");
                    if (originalHost == null) {
                        originalHost = request.getHeaders().getFirst(HttpHeaders.HOST);
                    }
                    
                    // Replace any internal service URLs with the gateway URL
                    String newLocation = location;
                    
                    // Replace web-service internal URLs
                    if (location.contains("web-service:8086")) {
                        newLocation = location.replace("web-service:8086", originalHost);
                    }
                    
                    // Replace localhost:8086 with the gateway host
                    if (location.contains("localhost:8086")) {
                        newLocation = location.replace("localhost:8086", originalHost);
                    }
                    
                    // Ensure the scheme is correct
                    if (newLocation.startsWith("http://") && "https".equals(originalScheme)) {
                        newLocation = newLocation.replace("http://", "https://");
                    }
                    
                    // Only update if we made changes
                    if (!location.equals(newLocation)) {
                        response.getHeaders().set(HttpHeaders.LOCATION, newLocation);
                    }
                }
            }
        }));
    }

    @Override
    public int getOrder() {
        return -1; // Execute before other filters
    }
}
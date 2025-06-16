package ua.nure.beautysalon.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LocationRewriteFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            HttpHeaders headers = response.getHeaders();
            
            String location = headers.getFirst(HttpHeaders.LOCATION);
            if (location != null && location.contains("web-service:8086")) {
                // Replace internal service name with external host
                String originalHost = exchange.getRequest().getHeaders().getFirst(HttpHeaders.HOST);
                if (originalHost != null) {
                    String newLocation = location.replace("web-service:8086", originalHost);
                    headers.set(HttpHeaders.LOCATION, newLocation);
                }
            }
        }));
    }

    @Override
    public int getOrder() {
        return -1; // Run before other filters
    }
}
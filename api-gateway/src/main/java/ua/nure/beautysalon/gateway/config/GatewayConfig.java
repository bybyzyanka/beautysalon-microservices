package com.pjatk.beautysalon.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Web Service routes
                .route("web-service-root", r -> r.path("/")
                        .filters(f -> f.preserveHostHeader())
                        .uri("lb://web-service"))

                .route("web-service-login", r -> r.path("/login")
                        .filters(f -> f.preserveHostHeader())
                        .uri("lb://web-service"))

                .route("web-service-pages", r -> r.path("/schedule", "/clients", "/masters", "/facilities")
                        .filters(f -> f.preserveHostHeader())
                        .uri("lb://web-service"))

                // Static resources
                .route("web-service-static", r -> r.path("/css/**", "/js/**", "/images/**")
                        .filters(f -> f.preserveHostHeader())
                        .uri("lb://web-service"))

                // API routes
                .route("user-service-api", r -> r.path("/api/auth/**")
                        .uri("lb://user-service"))

                .route("master-service-api", r -> r.path("/api/master/**")
                        .uri("lb://master-service"))

                .route("client-service-api", r -> r.path("/api/client/**")
                        .uri("lb://client-service"))

                .route("facility-service-api", r -> r.path("/api/facility/**")
                        .uri("lb://facility-service"))

                .route("schedule-service-api", r -> r.path("/api/schedule/**")
                        .uri("lb://schedule-service"))

                .build();
    }
}
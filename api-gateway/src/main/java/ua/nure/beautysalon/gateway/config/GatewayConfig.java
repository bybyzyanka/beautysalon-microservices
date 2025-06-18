package ua.nure.beautysalon.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // API routes - forward all requests as-is (no auth filters)
                .route("master-service-api", r -> r.path("/api/master/**")
                        .uri("lb://master-service"))

                .route("client-service-api", r -> r.path("/api/client/**")
                        .uri("lb://client-service"))

                .route("facility-service-api", r -> r.path("/api/facility/**")
                        .uri("lb://facility-service"))

                .route("schedule-service-api", r -> r.path("/api/schedule/**")
                        .uri("lb://schedule-service"))

                // Web Service routes - static resources
                .route("web-service-static", r -> r.path("/css/**", "/js/**", "/images/**", "/favicon.ico")
                        .uri("lb://web-service"))

                // Web Service pages - all pages publicly accessible
                .route("web-service-pages", r -> r.path("/", "/schedule", "/clients", "/masters", "/facilities")
                        .uri("lb://web-service"))

                .build();
    }
}
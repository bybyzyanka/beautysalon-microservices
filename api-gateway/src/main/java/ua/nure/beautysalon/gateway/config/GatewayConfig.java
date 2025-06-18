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
                // API routes - these should be handled by individual services
                .route("user-service-api", r -> r.path("/api/auth/**")
                        .filters(f -> f.removeRequestHeader("Authorization"))
                        .uri("lb://user-service"))

                .route("master-service-api", r -> r.path("/api/master/**")
                        .filters(f -> f.removeRequestHeader("Authorization"))
                        .uri("lb://master-service"))

                .route("client-service-api", r -> r.path("/api/client/**")
                        .filters(f -> f.removeRequestHeader("Authorization"))
                        .uri("lb://client-service"))

                .route("facility-service-api", r -> r.path("/api/facility/**")
                        .filters(f -> f.removeRequestHeader("Authorization"))
                        .uri("lb://facility-service"))

                .route("schedule-service-api", r -> r.path("/api/schedule/**")
                        .filters(f -> f.removeRequestHeader("Authorization"))
                        .uri("lb://schedule-service"))

                // Web Service routes - these handle the UI and authentication
                .route("web-service-static", r -> r.path("/css/**", "/js/**", "/images/**")
                        .filters(f -> f.preserveHostHeader())
                        .uri("lb://web-service"))

                .route("web-service-login", r -> r.path("/login")
                        .filters(f -> f.preserveHostHeader())
                        .uri("lb://web-service"))

                .route("web-service-logout", r -> r.path("/logout")
                        .filters(f -> f.preserveHostHeader())
                        .uri("lb://web-service"))

                .route("web-service-test", r -> r.path("/test/**")
                        .filters(f -> f.preserveHostHeader())
                        .uri("lb://web-service"))

                // Web Service protected pages
                .route("web-service-pages", r -> r.path("/", "/schedule", "/clients", "/masters", "/facilities")
                        .filters(f -> f.preserveHostHeader())
                        .uri("lb://web-service"))

                .build();
    }
}
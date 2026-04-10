package com.fitness.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class RoutingConfig {
    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
        return builder.routes()

                // Route 1 → Activity Service
                .route("activity-service", r -> r
                        .path("/api/activities/**")   // incoming path
                        .uri("http://localhost:8084") // target service
                )

                // Route 2 → User Service
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .uri("http://localhost:8081")
                )

                // Route 3 → Strip prefix example
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f.stripPrefix(1)) // /api remove karega
                        .uri("http://localhost:8082")
                )

                .build();
    }
}
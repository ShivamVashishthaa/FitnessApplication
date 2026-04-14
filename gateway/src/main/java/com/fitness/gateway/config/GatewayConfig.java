package com.fitness.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
        return builder.routes()

                .route("user-service", r -> r
                        .path("/api/users/**")
                        .uri("lb://USER-SERVICE")
                )
                .route("activity-service", r -> r
                        .path("/api/activities/**")
                        .uri("lb://ACTIVITY-SERVICE")
                )
                .route("ai-service", r -> r
                        .path("/api/recommendations/**")
                        .uri("lb://AI-SERVICE")
                )
                .build();
    }
}
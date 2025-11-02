package com.innowise.gatewayservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Gateway routes.
 * Defines routing rules programmatically, allowing URI override via environment variables.
 */
@Configuration
@RequiredArgsConstructor
public class GatewayRoutesConfig {

    @Value("${services.user-service.uri}")
    private String userServiceUri;

    @Value("${services.authentication-service.uri}")
    private String authenticationServiceUri;

    @Value("${services.order-service.uri}")
    private String orderServiceUri;

    private final JwtAuthenticationGatewayFilter jwtAuthenticationGatewayFilter;

    /**
     * Configures all gateway routes programmatically.
     *
     * @param builder The {@link RouteLocatorBuilder} for creating routes.
     * @return The configured {@link RouteLocator} with all service routes.
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", r -> r
                        .path("/api/v1/users/**", "/api/v1/cards/**")
                        .filters(f -> f.filter(jwtAuthenticationGatewayFilter
                                .apply(new JwtAuthenticationGatewayFilter.Config())))
                        .uri(userServiceUri)
                )
                .route("authentication-service-no-filters", r -> r
                        .path("/api/v1/auth/registration", "/api/v1/auth/login")
                        .uri(authenticationServiceUri)
                )
                .route("authentication-service", r -> r
                        .path("/api/v1/auth/**")
                        .filters(f -> f.filter(jwtAuthenticationGatewayFilter
                                .apply(new JwtAuthenticationGatewayFilter.Config())))
                        .uri(authenticationServiceUri)
                )
                .route("order-service", r -> r
                        .path("/api/v1/orders/**")
                        .filters(f -> f.filter(jwtAuthenticationGatewayFilter
                                .apply(new JwtAuthenticationGatewayFilter.Config())))
                        .uri(orderServiceUri)
                )
                .build();
    }

}

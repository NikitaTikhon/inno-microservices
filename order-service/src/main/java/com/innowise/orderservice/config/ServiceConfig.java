package com.innowise.orderservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

/**
 * Configuration class for service-level beans.
 * Configures REST template for inter-service communication with JWT authentication.
 */
@Configuration
@RequiredArgsConstructor
public class ServiceConfig {

    private final RestClientResponseErrorHandler restClientResponseErrorHandler;

    /**
     * Creates a configured {@link RestTemplate} bean for communication with the User Service.
     * The template includes JWT token propagation via an interceptor and custom error handling.
     *
     * @return The configured {@link RestTemplate} for user service REST calls.
     */
    @Bean
    public RestTemplate userServiceRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(new JwtClientHttpRequestInterceptor()));
        restTemplate.setErrorHandler(restClientResponseErrorHandler);

        return restTemplate;
    }

}

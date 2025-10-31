package com.innowise.authenticationservice.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;

/**
 * Configuration class for service-level beans, particularly Spring Security configuration.
 * Configures security filter chain for authentication endpoints and password encoding.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class ServiceConfig {

    private final RestClientResponseErrorHandler restClientResponseErrorHandler;

    /**
     * Configures the security filter chain for HTTP requests.
     * Disables CSRF protection, permits all requests to authentication endpoints,
     * and sets session management to stateless.
     *
     * @param http The {@link HttpSecurity} to configure.
     * @return The configured {@link SecurityFilterChain}.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests.requestMatchers("/api/v1/auth/**").permitAll()
                )
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    /**
     * Creates a BCrypt password encoder bean with strength 10 and secure random salt generation.
     * Each password will be hashed with a unique salt.
     *
     * @return The configured {@link PasswordEncoder}.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10, new SecureRandom());
    }

    /**
     * Creates a configured {@link RestTemplate} bean for communication with the User Service.
     *
     * @return The configured {@link RestTemplate} for user service REST calls.
     */
    @Bean
    public RestTemplate userServiceRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(restClientResponseErrorHandler);

        return restTemplate;
    }

}

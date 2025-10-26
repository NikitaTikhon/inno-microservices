package com.innowise.orderservice.config;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static com.innowise.orderservice.config.constant.SecurityConstant.AUTHORIZATION_BEARER_PREFIX;
import static com.innowise.orderservice.config.constant.SecurityConstant.AUTHORIZATION_HEADER;

/**
 * HTTP request interceptor that propagates JWT tokens to outgoing REST client requests.
 * Extracts the JWT token from the current security context and adds it to the Authorization header.
 */
public class JwtClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() != null) {
            Object credentials = authentication.getCredentials();

            if (credentials instanceof String token && !token.isBlank()) {

                request.getHeaders().set(AUTHORIZATION_HEADER, AUTHORIZATION_BEARER_PREFIX + token);
            }
        }

        return execution.execute(request, body);
    }

}

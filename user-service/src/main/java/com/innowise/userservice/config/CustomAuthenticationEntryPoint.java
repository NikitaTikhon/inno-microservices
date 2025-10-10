package com.innowise.userservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.userservice.model.dto.ErrorApiDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Custom authentication entry point for handling authentication failures.
 * This component is invoked when a user tries to access a secured REST resource without proper authentication.
 * Returns a standardized JSON error response instead of the default HTML error page.
 */
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        ErrorApiDto errorApiDto = ErrorApiDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message("Unauthorized")
                .path(request.getRequestURI())
                .build();
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        objectMapper.writeValue(response.getWriter(), errorApiDto);
    }

}

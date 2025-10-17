package com.innowise.orderservice.unit.controller;


import com.innowise.orderservice.controller.ErrorHandler;
import com.innowise.orderservice.exception.ResourceNotFoundException;
import com.innowise.orderservice.model.dto.ErrorApiDto;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErrorHandlerTest {

    @InjectMocks
    private ErrorHandler errorHandler;

    @Mock
    private HttpServletRequest request;

    @Test
    @DisplayName("Should handle ResourceNotFoundException and return 404")
    void handleResourceNotFoundException_ShouldReturn404_WhenResourceNotFoundException() {
        String message = "Order not found";
        String requestUri = "/api/v1/orders/1";
        ResourceNotFoundException exception = new ResourceNotFoundException(message);

        when(request.getRequestURI()).thenReturn(requestUri);

        ResponseEntity<ErrorApiDto> response = errorHandler.handleNotFoundException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).isEqualTo(message);
        assertThat(response.getBody().getPath()).isEqualTo(requestUri);
    }

    @Test
    @DisplayName("Should handle uncaught exception and return 500")
    void handleUncaughtException_ShouldReturn500_WhenAnyException() {
        String message = "Unexpected error";
        String requestUri = "/api/v1/orders";
        RuntimeException exception = new RuntimeException(message);

        when(request.getRequestURI()).thenReturn(requestUri);

        ResponseEntity<ErrorApiDto> response = errorHandler.handleUncaughtException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
        assertThat(response.getBody().getPath()).isEqualTo(requestUri);
    }

    @Test
    @DisplayName("Should handle access denied exception and return 403")
    void handleAccessDeniedException_ShouldReturn403_WhenAccessDeniedException() {
        String message = "Access denied";
        String requestUri = "/api/v1/orders";
        AccessDeniedException exception = new AccessDeniedException(message);

        when(request.getRequestURI()).thenReturn(requestUri);

        ResponseEntity<ErrorApiDto> response = errorHandler.handleAccessDeniedException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(403);
        assertThat(response.getBody().getError()).isEqualTo("Forbidden");
        assertThat(response.getBody().getMessage()).isEqualTo(message);
        assertThat(response.getBody().getPath()).isEqualTo(requestUri);
    }

    @Test
    @DisplayName("Should handle token invalid exception and return 401")
    void handleTokenInvalidException_ShouldReturn401_WhenTokenInvalidException() {
        String message = "Jwt exception";
        String requestUri = "/api/v1/orders";
        JwtException exception = new JwtException(message);

        when(request.getRequestURI()).thenReturn(requestUri);

        ResponseEntity<ErrorApiDto> response = errorHandler.handleTokenInvalidException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(401);
        assertThat(response.getBody().getError()).isEqualTo("Unauthorized");
        assertThat(response.getBody().getMessage()).isEqualTo(message);
        assertThat(response.getBody().getPath()).isEqualTo(requestUri);
    }

}

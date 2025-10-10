package com.innowise.userservice.unit.controller;

import com.innowise.userservice.controller.ErrorHandler;
import com.innowise.userservice.exception.CardNotFoundException;
import com.innowise.userservice.exception.MissingRequestParameterException;
import com.innowise.userservice.exception.UserAlreadyExistException;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.model.dto.ErrorApiDto;
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
    @DisplayName("Should handle UserNotFoundException and return 404")
    void handleNotFoundException_ShouldReturn404_WhenUserNotFoundException() {
        String message = "User not found";
        String requestUri = "/api/v1/users/1";
        UserNotFoundException exception = new UserNotFoundException(message);

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
    @DisplayName("Should handle CardNotFoundException and return 404")
    void handleNotFoundException_ShouldReturn404_WhenCardNotFoundException() {
        String message = "Card not found";
        String requestUri = "/api/v1/cards/1";
        CardNotFoundException exception = new CardNotFoundException(message);

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
    @DisplayName("Should handle UserAlreadyExistException and return 409")
    void handleConflictException_ShouldReturn409_WhenUserAlreadyExistException() {
        String message = "User with email already exists";
        String requestUri = "/api/v1/users";
        UserAlreadyExistException exception = new UserAlreadyExistException(message);

        when(request.getRequestURI()).thenReturn(requestUri);

        ResponseEntity<ErrorApiDto> response = errorHandler.handleConflictException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getError()).isEqualTo("Conflict");
        assertThat(response.getBody().getMessage()).isEqualTo(message);
        assertThat(response.getBody().getPath()).isEqualTo(requestUri);
    }

    @Test
    @DisplayName("Should handle MissingRequestParameterException and return 400")
    void handleMissingRequestParameterException_ShouldReturn400_WhenMissingRequestParameterException() {
        String message = "Missing request parameter: ids";
        String requestUri = "/api/v1/users";
        MissingRequestParameterException exception = new MissingRequestParameterException(message);

        when(request.getRequestURI()).thenReturn(requestUri);

        ResponseEntity<ErrorApiDto> response = errorHandler.handleMissingRequestParameterException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).isEqualTo(message);
        assertThat(response.getBody().getPath()).isEqualTo(requestUri);
    }

    @Test
    @DisplayName("Should handle uncaught exception and return 500")
    void handleUncaughtException_ShouldReturn500_WhenAnyException() {
        String message = "Unexpected error";
        String requestUri = "/api/v1/users";
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
        String requestUri = "/api/v1/users";
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
        String requestUri = "/api/v1/users";
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

package com.innowise.orderservice.controller;

import com.innowise.orderservice.exception.ExternalServiceException;
import com.innowise.orderservice.exception.ResourceNotFoundException;
import com.innowise.orderservice.model.dto.ErrorApiDto;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A global exception handler for the REST controllers.
 * This class provides centralized exception handling across all controllers in the application.
 * It catches specific exceptions and returns a consistent {@link ErrorApiDto} response.
 */
@Slf4j
@RestControllerAdvice
public class ErrorHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles all other uncaught exceptions (the catch-all handler).
     * This method ensures that every unhandled exception returns a standardized
     * 500 Internal Server Error response.
     *
     * @param ex The {@link Exception} that was thrown.
     * @param request The current {@link HttpServletRequest}.
     * @return A {@link ResponseEntity} with a 500 status and the ErrorApiDto.
     */
     @ExceptionHandler(Throwable.class)
     public ResponseEntity<ErrorApiDto> handleUncaughtException(Throwable ex, HttpServletRequest request) {
         log.error(ex.getMessage(), ex);

         ErrorApiDto errorApiDto = ErrorApiDto.builder()
                 .timestamp(LocalDateTime.now())
                 .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                 .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                 .message("An unexpected error occurred")
                 .path(request.getRequestURI())
                 .build();

         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorApiDto);
     }

    /**
     * Handles validation exceptions thrown by `@Valid` annotation.
     * It extracts field-level validation errors and formats them into a
     * structured error response.
     *
     * @param ex The {@link MethodArgumentNotValidException} that was thrown.
     * @param request The current {@link HttpServletRequest}.
     * @return A {@link ResponseEntity} containing an {@link ErrorApiDto} with a
     * BAD_REQUEST status (400) and detailed validation errors.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing + "\n " + replacement
                ));

        ErrorApiDto errorApiDto = ErrorApiDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Validation Failed")
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .errors(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorApiDto);
    }

    /**
     * Handles exceptions when the request body cannot be read or deserialized.
     *
     * @param ex The {@link HttpMessageNotReadableException} that was thrown.
     * @param headers The HTTP headers.
     * @param status The HTTP status code.
     * @param request The current {@link WebRequest}.
     * @return A {@link ResponseEntity} containing an {@link ErrorApiDto} with a
     * BAD_REQUEST status (400) and a detailed error message.
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ErrorApiDto errorApiDto = ErrorApiDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorApiDto);
    }

    /**
     * Handles exceptions when a method argument type conversion fails.
     * This typically occurs with path variables or request parameters when
     * the provided value cannot be converted to the expected type.
     *
     * @param ex The {@link MethodArgumentTypeMismatchException} that was thrown.
     * @param request The current {@link HttpServletRequest}.
     * @return A {@link ResponseEntity} containing an {@link ErrorApiDto} with a
     * BAD_REQUEST status (400) and a detailed error message.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorApiDto> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        ErrorApiDto errorApiDto = ErrorApiDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorApiDto);
    }

    /**
     * Handles exceptions when a requested resource is not found.
     * Catches the {@link ResourceNotFoundException}
     *
     * @param ex The {@link RuntimeException} ({@link ResourceNotFoundException}.
     * @param request The current {@link HttpServletRequest}.
     * @return A {@link ResponseEntity} containing an {@link ErrorApiDto} with a
     * NOT_FOUND status (404).
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorApiDto> handleNotFoundException(RuntimeException ex, HttpServletRequest request) {
        ErrorApiDto errorApiDto = ErrorApiDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorApiDto);
    }

    /**
     * Handles access denied exceptions when a user lacks sufficient permissions.
     * Catches {@link AccessDeniedException} thrown by Spring Security when a user
     * tries to access a resource they don't have permission for (e.g., missing required role).
     *
     * @param ex The {@link RuntimeException} ({@link AccessDeniedException}).
     * @param request The current {@link HttpServletRequest}.
     * @return A {@link ResponseEntity} containing an {@link ErrorApiDto} with a FORBIDDEN status (403).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorApiDto> handleAccessDeniedException(RuntimeException ex, HttpServletRequest request) {
        ErrorApiDto errorApiDto = ErrorApiDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorApiDto);
    }

    /**
     * Handles JWT and authentication-related exceptions.
     * Catches {@link JwtException} (token parsing/validation errors).
     *
     * @param ex The {@link RuntimeException} {@link JwtException}
     * @param request The current {@link HttpServletRequest}.
     * @return A {@link ResponseEntity} containing an {@link ErrorApiDto} with an UNAUTHORIZED status (401).
     */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorApiDto> handleTokenInvalidException(RuntimeException ex, HttpServletRequest request) {
        ErrorApiDto errorApiDto = ErrorApiDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorApiDto);
    }

    /**
     * Handles service unavailability exceptions when external services cannot be reached.
     * This includes both connection failures and circuit breaker protections.
     * Catches:
     * - {@link ResourceAccessException} - connection failures (timeout, refused, network errors)
     * - {@link CallNotPermittedException} - circuit breaker is open due to high error rate
     *
     * @param ex The exception that was thrown.
     * @param request The current {@link HttpServletRequest}.
     * @return A {@link ResponseEntity} containing an {@link ErrorApiDto} with a SERVICE_UNAVAILABLE status (503).
     */
    @ExceptionHandler({ResourceAccessException.class, CallNotPermittedException.class})
    public ResponseEntity<ErrorApiDto> handleServiceUnavailableException(Exception ex, HttpServletRequest request) {
        ErrorApiDto errorApiDto = ErrorApiDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .error(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorApiDto);
    }

    /**
     * Handles external service exceptions when a remote service returns an error response.
     * This typically occurs when a remote service returns an unexpected error status code
     * (e.g., 500 Internal Server Error from external service).
     * Catches {@link ExternalServiceException} thrown by the REST client error handler.
     *
     * @param ex The {@link ExternalServiceException} that was thrown.
     * @param request The current {@link HttpServletRequest}.
     * @return A {@link ResponseEntity} containing an {@link ErrorApiDto} with a BAD_GATEWAY status (502).
     */
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorApiDto> handleExternalServiceException(ExternalServiceException ex, HttpServletRequest request) {
        ErrorApiDto errorApiDto = ErrorApiDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_GATEWAY.value())
                .error(HttpStatus.BAD_GATEWAY.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorApiDto);
    }

}

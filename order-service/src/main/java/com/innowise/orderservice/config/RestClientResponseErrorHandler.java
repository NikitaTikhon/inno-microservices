package com.innowise.orderservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.orderservice.exception.ExternalServiceException;
import com.innowise.orderservice.exception.ResourceNotFoundException;
import com.innowise.orderservice.model.dto.ErrorApiDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.net.URI;

/**
 * Custom error handler for REST client responses.
 * Handles HTTP errors from remote service calls and converts them to appropriate exceptions.
 */
@Component
@RequiredArgsConstructor
public class RestClientResponseErrorHandler implements ResponseErrorHandler {

    private final ObjectMapper objectMapper;

    /**
     * Determines if the response has an error by checking for 4xx or 5xx status codes.
     *
     * @param response The client HTTP response.
     * @return {@code true} if the response has a 4xx or 5xx status code, {@code false} otherwise.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().is4xxClientError()
                || response.getStatusCode().is5xxServerError();
    }

    /**
     * Handles errors in the HTTP response by parsing the error body and throwing appropriate exceptions.
     * Converts NOT_FOUND (404) to {@link ResourceNotFoundException},
     * FORBIDDEN (403) to {@link AccessDeniedException},
     * BAD_REQUEST (400) to {@link IllegalArgumentException},
     * and other errors to {@link ExternalServiceException}.
     *
     * @param url The request URL.
     * @param method The HTTP method used.
     * @param response The client HTTP response.
     * @throws IOException if an I/O error occurs or the error body cannot be read.
     * @throws ResourceNotFoundException if the remote service returns 404.
     * @throws AccessDeniedException if the remote service returns 403.
     * @throws IllegalArgumentException if the remote service returns 400.
     * @throws ExternalServiceException for other error status codes.
     */
    @Override
    public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
        HttpStatus statusCode = HttpStatus.valueOf(response.getStatusCode().value());
        String errorMessage;

        ErrorApiDto errorApiDto = parseErrorBody(response);
        
        if (errorApiDto != null && errorApiDto.getMessage() != null) {
            errorMessage = errorApiDto.getMessage();
        } else {
            errorMessage = "Remote service error: %s".formatted(statusCode.getReasonPhrase());
        }

        if (statusCode == HttpStatus.NOT_FOUND) {
            throw new ResourceNotFoundException(errorMessage);
        }

        if (statusCode == HttpStatus.FORBIDDEN) {
            throw new AccessDeniedException(errorMessage);
        }

        if (statusCode == HttpStatus.BAD_REQUEST) {
            throw new IllegalArgumentException(errorMessage);
        }

        String detailedMessage = "Remote service returned %d %s: %s".formatted(statusCode.value(), statusCode.getReasonPhrase(), errorMessage);
        throw new ExternalServiceException(detailedMessage);
    }

    /**
     * Attempts to parse the error response body as an ErrorApiDto.
     * Returns null if parsing fails.
     *
     * @param response The client HTTP response.
     * @return The parsed {@link ErrorApiDto} or null if parsing failed.
     */
    private ErrorApiDto parseErrorBody(ClientHttpResponse response) {
        try {
            return objectMapper.readValue(response.getBody(), ErrorApiDto.class);
        } catch (IOException e) {
            return null;
        }
    }

}

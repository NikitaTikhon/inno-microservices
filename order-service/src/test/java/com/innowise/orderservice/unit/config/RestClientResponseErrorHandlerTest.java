package com.innowise.orderservice.unit.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.orderservice.config.RestClientResponseErrorHandler;
import com.innowise.orderservice.exception.ExternalServiceException;
import com.innowise.orderservice.exception.ResourceNotFoundException;
import com.innowise.orderservice.model.dto.ErrorApiDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.access.AccessDeniedException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestClientResponseErrorHandlerTest {

    private RestClientResponseErrorHandler errorHandler;

    @Mock
    private ClientHttpResponse response;

    private ObjectMapper objectMapper;
    private URI testUri;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        errorHandler = new RestClientResponseErrorHandler(objectMapper);
        testUri = URI.create("http://localhost:8081/api/users/123");
    }

    @Test
    @DisplayName("Should return true for 4xx and 5xx errors, false for 2xx success")
    void shouldDetectErrorStatusCodes() throws IOException {
        when(response.getStatusCode()).thenReturn(HttpStatusCode.valueOf(400));
        assertThat(errorHandler.hasError(response)).isTrue();

        when(response.getStatusCode()).thenReturn(HttpStatusCode.valueOf(500));
        assertThat(errorHandler.hasError(response)).isTrue();

        when(response.getStatusCode()).thenReturn(HttpStatusCode.valueOf(200));
        assertThat(errorHandler.hasError(response)).isFalse();
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when status is 404")
    void shouldThrowResourceNotFoundException_WhenStatusIs404() throws IOException {
        ErrorApiDto errorDto = ErrorApiDto.builder()
                .message("User not found")
                .build();

        InputStream inputStream = new ByteArrayInputStream(objectMapper.writeValueAsBytes(errorDto));

        when(response.getStatusCode()).thenReturn(HttpStatusCode.valueOf(404));
        when(response.getBody()).thenReturn(inputStream);

        assertThatThrownBy(() -> errorHandler.handleError(testUri, HttpMethod.GET, response))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when status is 403")
    void shouldThrowAccessDeniedException_WhenStatusIs403() throws IOException {
        ErrorApiDto errorDto = ErrorApiDto.builder()
                .message("Access denied")
                .build();

        InputStream inputStream = new ByteArrayInputStream(objectMapper.writeValueAsBytes(errorDto));

        when(response.getStatusCode()).thenReturn(HttpStatusCode.valueOf(403));
        when(response.getBody()).thenReturn(inputStream);

        assertThatThrownBy(() -> errorHandler.handleError(testUri, HttpMethod.GET, response))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Access denied");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when status is 400")
    void shouldThrowIllegalArgumentException_WhenStatusIs400() throws IOException {
        ErrorApiDto errorDto = ErrorApiDto.builder()
                .message("Invalid request")
                .build();

        InputStream inputStream = new ByteArrayInputStream(objectMapper.writeValueAsBytes(errorDto));

        when(response.getStatusCode()).thenReturn(HttpStatusCode.valueOf(400));
        when(response.getBody()).thenReturn(inputStream);

        assertThatThrownBy(() -> errorHandler.handleError(testUri, HttpMethod.GET, response))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid request");
    }

    @Test
    @DisplayName("Should throw ExternalServiceException when status is 500")
    void shouldThrowExternalServiceException_WhenStatusIs500() throws IOException {
        ErrorApiDto errorDto = ErrorApiDto.builder()
                .message("Database error")
                .build();

        InputStream inputStream = new ByteArrayInputStream(objectMapper.writeValueAsBytes(errorDto));

        when(response.getStatusCode()).thenReturn(HttpStatusCode.valueOf(500));
        when(response.getBody()).thenReturn(inputStream);

        assertThatThrownBy(() -> errorHandler.handleError(testUri, HttpMethod.GET, response))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessage("Remote service returned 500 Internal Server Error: Database error");
    }

    @Test
    @DisplayName("Should use default error message when error body is invalid or null")
    void shouldUseDefaultErrorMessage_WhenErrorBodyIsInvalidOrNull() throws IOException {
        String invalidJson = "{ invalid json }";
        InputStream inputStream = new ByteArrayInputStream(invalidJson.getBytes());

        when(response.getStatusCode()).thenReturn(HttpStatusCode.valueOf(500));
        when(response.getBody()).thenReturn(inputStream);

        assertThatThrownBy(() -> errorHandler.handleError(testUri, HttpMethod.GET, response))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessage("Remote service returned 500 Internal Server Error: Remote service error: Internal Server Error");
    }

}

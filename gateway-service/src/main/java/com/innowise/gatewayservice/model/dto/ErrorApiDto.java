package com.innowise.gatewayservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * A Data Transfer Object (DTO) that represents a standardized API error response.
 * This class is used to provide a consistent structure for error messages returned
 * by the API, making it easier for clients to handle and display errors.
 * <p>
 * The DTO includes details such as a timestamp, HTTP status, a general error message,
 * and a map for specific field validation errors.
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ErrorApiDto {

    /**
     * The timestamp when the error occurred.
     */
    private LocalDateTime timestamp;

    /**
     * The HTTP status code of the error (e.g., 404, 500).
     */
    private Integer status;

    /**
     * A general, short description of the error (e.g., "Not Found", "Bad Request").
     */
    private String error;

    /**
     * A more detailed message providing specific information about the error.
     */
    private String message;

    /**
     * The requested URI path that caused the error.
     */
    private String path;

    /**
     * A map of specific validation errors. The key is the name of the field, and the
     * value is the error message for that field. This is particularly useful for
     * form validation errors.
     */
    @Builder.Default
    private Map<String, String> errors = new HashMap<>();

}

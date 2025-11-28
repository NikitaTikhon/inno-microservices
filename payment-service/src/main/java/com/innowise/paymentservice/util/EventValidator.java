package com.innowise.paymentservice.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventValidator {

    private final Validator validator;

    public <T> void validate(T event) {
        if (event == null) {
            log.error("Event is null - deserialization failed");
            throw new IllegalArgumentException("Event cannot be null - deserialization failed");
        }
        
        Set<ConstraintViolation<T>> violations = validator.validate(event);
        
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            
            log.error("Validation failed for event {}: {}", event.getClass().getSimpleName(), errorMessage);
            throw new IllegalArgumentException("Invalid event: " + errorMessage);
        }
    }
    
}


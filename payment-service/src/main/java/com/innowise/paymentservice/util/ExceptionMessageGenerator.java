package com.innowise.paymentservice.util;


/**
 * Utility class responsible for generating formatted exception messages.
 * Centralizes exception message strings to ensure consistency across the application.
 */
public class ExceptionMessageGenerator {

    private static final String PAYMENT_NOT_FOUND = "Payment with %s: %s not found";

    private ExceptionMessageGenerator() {}

    public static String paymentNotFound(String field, String value) {
        return PAYMENT_NOT_FOUND.formatted(field, value);
    }

}

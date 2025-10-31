package com.innowise.gatewayservice.util;


/**
 * Utility class responsible for generating formatted exception messages.
 * Centralizes exception message strings to ensure consistency across the application.
 */
public class ExceptionMessageGenerator {

    private static final String MISSING_OR_INVALID_HEADER = "Missing or invalid Authorization header";

    private static final String TOKEN_EXPIRED = "Token expired";
    private static final String TOKEN_INVALID_CLAIMS = "Invalid token claims";

    private ExceptionMessageGenerator() {
    }

    public static String missingOrInvalidHeader() {
        return MISSING_OR_INVALID_HEADER;
    }

    public static String tokenExpired() {
        return TOKEN_EXPIRED;
    }

    public static String tokenInvalidClaims() {
        return TOKEN_INVALID_CLAIMS;
    }

}

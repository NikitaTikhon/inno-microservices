package com.innowise.authenticationservice.util;

import com.innowise.authenticationservice.model.RoleEnum;

/**
 * Utility class responsible for generating formatted exception messages.
 * Centralizes exception message strings to ensure consistency across the application.
 */
public class ExceptionMessageGenerator {

    private static final String USER_BAD_CREDENTIALS = "User bad credentials";
    private static final String USER_EMAIL_EXISTS = "User with email: %s already exists";

    private static final String ROLE_NOT_FOUND = "%s not found";

    private static final String AUTH_HEADER_WRONG = "Authorization header is wrong";
    private static final String AUTH_HEADER_MISSING = "Authorization header is missing";
    private static final String TOKEN_INVALID = "Token is invalid";

    private ExceptionMessageGenerator() {}

    public static String userBadCredentials() {
        return USER_BAD_CREDENTIALS;
    }

    public static String userExists(String email) {
        return USER_EMAIL_EXISTS.formatted(email);
    }

    public static String roleNotFound(RoleEnum roleEnum) {
        return ROLE_NOT_FOUND.formatted(roleEnum);
    }

    public static String authHeaderWrong() {
        return AUTH_HEADER_WRONG;
    }

    public static String authHeaderMissing() {
        return AUTH_HEADER_MISSING;
    }

    public static String tokenInvalid() {
        return TOKEN_INVALID;
    }

}

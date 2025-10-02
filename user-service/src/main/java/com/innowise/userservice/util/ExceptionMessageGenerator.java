package com.innowise.userservice.util;

/**
 * Utility class responsible for generating formatted exception messages.
 * Centralizes exception message strings to ensure consistency across the application.
 */
public class ExceptionMessageGenerator {

    private static final String USER_ID_NOT_FOUND = "User with id: %s not found";
    private static final String USER_EMAIL_NOT_FOUND = "User with email: %s not found";
    private static final String USER_EMAIL_EXIST = "User with email: %s already exists";
    private static final String CARD_ID_NOT_FOUND = "CardInfo with id: %s not found";

    private static final String MISSING_REQUEST_PARAMETER = "Required '%s' parameter is missing";

    private ExceptionMessageGenerator() {}

    public static String userNotFound(Long id) {
        return USER_ID_NOT_FOUND.formatted(id);
    }

    public static String userNotFound(String email) {
        return USER_EMAIL_NOT_FOUND.formatted(email);
    }

    public static String userExist(String email) {
        return USER_EMAIL_EXIST.formatted(email);
    }

    public static String cardNotFound(Long id) {
        return CARD_ID_NOT_FOUND.formatted(id);
    }

    public static String missingRequestParameter(String parameter) {
        return MISSING_REQUEST_PARAMETER.formatted(parameter);
    }

}

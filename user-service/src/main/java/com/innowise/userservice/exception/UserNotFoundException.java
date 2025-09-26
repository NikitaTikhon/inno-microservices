package com.innowise.userservice.exception;

public class UserNotFoundException extends RuntimeException {

    private static final String USER_NOT_FOUND_BY_ID = "User with id: %s not found";
    private static final String USER_NOT_FOUND_BY_EMAIL = "User with email: %s not found";

    public UserNotFoundException(Long id) {
        super(USER_NOT_FOUND_BY_ID.formatted(id));
    }

    public UserNotFoundException(String email) {
        super(USER_NOT_FOUND_BY_EMAIL.formatted(email));
    }

}

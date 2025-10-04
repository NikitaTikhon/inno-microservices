package com.innowise.userservice.exception;

import java.io.Serial;

public class UserAlreadyExistException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 8752327574034208449L;

    public UserAlreadyExistException(String message) {
        super(message);
    }

}

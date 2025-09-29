package com.innowise.userservice.exception;

import java.io.Serial;

public class UserNotFoundException extends NotFoundException {

    @Serial
    private static final long serialVersionUID = -4689573831713216639L;

    public UserNotFoundException(String message) {
        super(message);
    }

}

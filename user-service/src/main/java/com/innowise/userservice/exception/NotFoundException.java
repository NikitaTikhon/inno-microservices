package com.innowise.userservice.exception;

import java.io.Serial;

public class NotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 2817138428489063504L;

    public NotFoundException(String message) {
        super(message);
    }

}

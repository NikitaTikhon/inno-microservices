package com.innowise.orderservice.exception;

import java.io.Serial;

public class ResourceNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1208058532126723308L;

    public ResourceNotFoundException(String message) {
        super(message);
    }

}

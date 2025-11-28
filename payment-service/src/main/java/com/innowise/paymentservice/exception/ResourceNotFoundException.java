package com.innowise.paymentservice.exception;

import java.io.Serial;

public class ResourceNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -769440298069570672L;

    public ResourceNotFoundException(String message) {
        super(message);
    }

}

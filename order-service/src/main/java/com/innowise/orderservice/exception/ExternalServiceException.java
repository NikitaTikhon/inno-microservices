package com.innowise.orderservice.exception;

import java.io.Serial;


public class ExternalServiceException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1208058532126723309L;

    public ExternalServiceException(String message) {
        super(message);
    }

}


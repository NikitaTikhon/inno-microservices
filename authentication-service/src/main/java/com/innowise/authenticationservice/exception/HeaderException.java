package com.innowise.authenticationservice.exception;

import java.io.Serial;

public class HeaderException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 8764040291938208033L;

    public HeaderException(String message) {
        super(message);
    }

}

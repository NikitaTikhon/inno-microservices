package com.innowise.authenticationservice.exception;

import java.io.Serial;

public class ResourceAlreadyExistsException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -1787184307920256519L;

    public ResourceAlreadyExistsException(String message) {
        super(message);
    }

}

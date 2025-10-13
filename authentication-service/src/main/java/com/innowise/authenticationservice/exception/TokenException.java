package com.innowise.authenticationservice.exception;

import io.jsonwebtoken.JwtException;

import java.io.Serial;

public class TokenException extends JwtException {

    @Serial
    private static final long serialVersionUID = -6803405948565298681L;

    public TokenException(String message) {
        super(message);
    }

}

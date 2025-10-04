package com.innowise.userservice.exception;

public class MissingRequestParameterException extends RuntimeException {

    public MissingRequestParameterException(String message) {
        super(message);
    }

}

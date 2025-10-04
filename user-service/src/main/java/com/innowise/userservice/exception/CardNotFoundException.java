package com.innowise.userservice.exception;

import java.io.Serial;

public class CardNotFoundException extends NotFoundException {

    @Serial
    private static final long serialVersionUID = 6101639314352137138L;

    public CardNotFoundException(String message) {
        super(message);
    }

}

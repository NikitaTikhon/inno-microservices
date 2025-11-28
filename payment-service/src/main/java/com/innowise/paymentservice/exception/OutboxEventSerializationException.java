package com.innowise.paymentservice.exception;

import java.io.Serial;

public class OutboxEventSerializationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 7051990471862159778L;

    public OutboxEventSerializationException(String message) {
        super(message);
    }

}



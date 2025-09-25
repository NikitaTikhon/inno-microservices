package com.innowise.userservice.exception;

public class CardNotFoundException extends RuntimeException {

    private static final String CARD_NOT_FOUND_BY_ID = "CardInfo with id: %s not found";

    public CardNotFoundException(Long id) {
        super(CARD_NOT_FOUND_BY_ID.formatted(id));
    }

}

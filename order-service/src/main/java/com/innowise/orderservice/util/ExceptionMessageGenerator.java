package com.innowise.orderservice.util;


/**
 * Utility class responsible for generating formatted exception messages.
 * Centralizes exception message strings to ensure consistency across the application.
 */
public class ExceptionMessageGenerator {

    private static final String NOT_ALL_ITEMS_FOUND = "Not all items found";

    private static final String ORDER_NOT_FOUND = "Order with id: %s not found";

    private ExceptionMessageGenerator() {
    }

    public static String notAllItemsFound() {
        return NOT_ALL_ITEMS_FOUND;
    }

    public static String orderNotFound(Long id) {
        return ORDER_NOT_FOUND.formatted(id);
    }

}

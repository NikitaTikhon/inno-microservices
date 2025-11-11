package com.innowise.paymentservice.service;

import com.innowise.paymentservice.model.PaymentStatus;
import com.innowise.paymentservice.model.dto.PaymentRequest;
import com.innowise.paymentservice.model.dto.PaymentResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for managing payment operations.
 * Provides methods for creating, retrieving, and analyzing payment data.
 */
public interface PaymentService {

    /**
     * Creates and saves a new payment based on the provided request.
     *
     * @param paymentRequest The payment request containing payment details.
     * @return The created payment as {@link PaymentResponse}.
     */
    PaymentResponse save(PaymentRequest paymentRequest);

    /**
     * Retrieves all payments associated with a specific order.
     *
     * @param orderId The order ID to search for.
     * @return A {@link List} of payments for the given order.
     */
    List<PaymentResponse> findByOrderId(Long orderId);

    /**
     * Retrieves all payments made by a specific user.
     *
     * @param userId The user ID to search for.
     * @return A {@link List} of payments made by the user.
     */
    List<PaymentResponse> findByUserId(Long userId);

    /**
     * Retrieves all payments that match any of the provided statuses.
     *
     * @param statuses The list of payment statuses to filter by.
     * @return A {@link List} of payments matching any of the provided statuses.
     */
    List<PaymentResponse> findByStatuses(List<PaymentStatus> statuses);

    /**
     * Calculates the total amount of all payments within a specified time range.
     *
     * @param from The start of the time range (inclusive).
     * @param to The end of the time range (inclusive).
     * @return The total amount as {@link BigDecimal}.
     */
    BigDecimal findTotalAmount(LocalDateTime from, LocalDateTime to);

}

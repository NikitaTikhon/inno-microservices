package com.innowise.paymentservice.service;

import com.innowise.paymentservice.model.PaymentStatus;

/**
 * Service responsible for processing payment requests and determining payment status.
 */
public interface PaymentProcessorService {

    /**
     * Processes a payment and determines its status.
     * <p>
     * The method retrieves a random number from an external API and determines
     * the payment status based on whether the number is even or odd.
     * </p>
     *
     * @return {@link PaymentStatus#SUCCESS} if the random number is even,
     *         {@link PaymentStatus#FAILED} if the random number is odd
     */
    PaymentStatus processPayment();

}

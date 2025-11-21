package com.innowise.paymentservice.service;

import com.innowise.paymentservice.model.dto.CreatePaymentEvent;

/**
 * Service interface for managing Outbox Event operations.
 * <p>
 * Events are stored with {@code PENDING} status and later processed by
 * {@link OutboxEventScheduler} which attempts to publish them to Kafka.
 * </p>
 */
public interface OutboxEventService {

    /**
     * Saves a payment event to the outbox table with PENDING status.
     *
     * @param event the payment event to be saved and eventually published
     * @throws com.innowise.paymentservice.exception.OutboxEventSerializationException
     *         if event serialization fails
     */
    void save(CreatePaymentEvent event);

    /**
     * Checks if an outbox event exists for the given order ID.
     *
     * @param orderId the order identifier to check
     * @return {@code true} if an outbox event exists for the order, {@code false} otherwise
     */
    boolean existsByOrderId(Long orderId);

}

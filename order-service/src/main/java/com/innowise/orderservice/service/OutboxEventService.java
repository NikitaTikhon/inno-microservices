package com.innowise.orderservice.service;


import com.innowise.orderservice.model.dto.CreateOrderEvent;

/**
 * Service interface for managing Outbox Event operations.
 * <p>
 * Events are stored with {@code PENDING} status and later processed by
 * {@link OutboxEventScheduler} which attempts to publish them to Kafka.
 * </p>
 */
public interface OutboxEventService {

    /**
     * Saves a order event to the outbox table with PENDING status.
     *
     * @param event the order event to be saved and eventually published
     * @throws com.innowise.orderservice.exception.OutboxEventSerializationException
     *         if event serialization fails
     */
    void save(CreateOrderEvent event);

}

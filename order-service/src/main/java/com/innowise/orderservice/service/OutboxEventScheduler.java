package com.innowise.orderservice.service;

/**
 * Scheduler service for processing pending Outbox Events.
 * Publishes events to Kafka.
 */
public interface OutboxEventScheduler {

    /**
     * Processes pending outbox events by attempting to publish them to Kafka.
     */
    void processOutboxEvents();

}

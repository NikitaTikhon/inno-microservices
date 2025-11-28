package com.innowise.orderservice.service;

import com.innowise.orderservice.model.dto.CreateOrderEvent;
import com.innowise.orderservice.model.dto.CreatePaymentEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * Service interface for Kafka message operations in Order Service.
 */
public interface KafkaService {

    /**
     * Consumes CREATE_PAYMENT event from Kafka topic and updates order status accordingly.
     *
     * @param consumerRecord The Kafka {@link ConsumerRecord} containing {@link CreatePaymentEvent} with payment details.
     */
    void consumeCreatePaymentEvent(ConsumerRecord<String, CreatePaymentEvent> consumerRecord);

    /**
     * Sends CREATE_ORDER event to Kafka topic for Payment Service to process.
     *
     *
     * @param event The {@link CreateOrderEvent} containing order information.
     */
    void sendCreateOrderEvent(CreateOrderEvent event);

}

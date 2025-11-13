package com.innowise.paymentservice.service;

import com.innowise.paymentservice.model.dto.CreatePaymentEvent;
import com.innowise.paymentservice.model.dto.PaymentRequest;
import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * Service interface for Kafka message operations in Payment Service.
 */
public interface KafkaService {

    /**
     * Consumes CREATE_ORDER event from Kafka topic and creates a corresponding payment.
     *
     * @param consumerRecord The Kafka {@link ConsumerRecord} containing {@link PaymentRequest} with order details.
     */
    void consumeCreateOrderEvent(ConsumerRecord<String, PaymentRequest> consumerRecord);

    /**
     * Sends CREATE_PAYMENT event to Kafka topic for Order Service to process.
     *
     * @param event The {@link CreatePaymentEvent} containing payment information.
     */
    void sendCreatePaymentEvent(CreatePaymentEvent event);

}

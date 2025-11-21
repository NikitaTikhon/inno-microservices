package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.config.KafkaConfig;
import com.innowise.paymentservice.model.dto.CreatePaymentEvent;
import com.innowise.paymentservice.model.dto.PaymentRequest;
import com.innowise.paymentservice.model.dto.PaymentResponse;
import com.innowise.paymentservice.service.KafkaService;
import com.innowise.paymentservice.service.OutboxEventService;
import com.innowise.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaServiceImpl implements KafkaService {

    private final KafkaTemplate<String, CreatePaymentEvent> kafkaTemplate;

    private final PaymentService paymentService;
    private final OutboxEventService outboxEventService;

    @Override
    @KafkaListener(topics = KafkaConfig.ORDER_CREATED_TOPIC, groupId = KafkaConfig.PAYMENT_SERVICE_ORDER_CONSUMER_GROUP)
    public void consumeCreateOrderEvent(ConsumerRecord<String, PaymentRequest> consumerRecord) {
        PaymentRequest event = consumerRecord.value();
        if (event == null) {
            log.error("Failed to deserialize PaymentRequest at partition={}, offset={}",
                    consumerRecord.partition(), consumerRecord.offset());
            throw new IllegalArgumentException("Invalid PaymentRequest - cannot deserialize");
        }

        if (handleExistingPayment(event.getOrderId())) {
            return;
        }

        paymentService.save(event);
    }

    private boolean handleExistingPayment(Long orderId) {
        if (!paymentService.existsByOrderId(orderId)) {
            return false;
        }

        if (outboxEventService.existsByOrderId(orderId)) {
            log.info("Payment and OutboxEvent already exist for order {}, skipping", orderId);
            return true;
        }

        log.warn("Payment exists but OutboxEvent missing for order {}, recovering...", orderId);
        recoverMissingOutboxEvent(orderId);
        return true;
    }

    private void recoverMissingOutboxEvent(Long orderId) {
        PaymentResponse paymentResponse = paymentService.findByOrderId(orderId);
        CreatePaymentEvent createPaymentEvent = CreatePaymentEvent.builder()
                .orderId(paymentResponse.getOrderId())
                .status(paymentResponse.getStatus())
                .build();

        outboxEventService.save(createPaymentEvent);
    }

    @Override
    public void sendCreatePaymentEvent(CreatePaymentEvent event) {
        try {
            kafkaTemplate.send(KafkaConfig.PAYMENT_CREATED_TOPIC, event.getOrderId().toString(), event)
                    .get(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new KafkaException("Thread interrupted while sending CREATE_PAYMENT event", e);
        } catch (Exception e) {
            throw new KafkaException("Failed to send CREATE_PAYMENT event", e);
        }
    }

}

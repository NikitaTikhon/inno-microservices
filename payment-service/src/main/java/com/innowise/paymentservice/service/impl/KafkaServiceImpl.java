package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.config.KafkaConfig;
import com.innowise.paymentservice.model.dto.CreatePaymentEvent;
import com.innowise.paymentservice.model.dto.PaymentRequest;
import com.innowise.paymentservice.model.dto.PaymentResponse;
import com.innowise.paymentservice.service.KafkaService;
import com.innowise.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaServiceImpl implements KafkaService {

    private final KafkaTemplate<String, CreatePaymentEvent> kafkaTemplate;

    private final PaymentService paymentService;

    @Override
    @KafkaListener(topics = KafkaConfig.ORDER_CREATED_TOPIC, groupId = KafkaConfig.PAYMENT_SERVICE_ORDER_CONSUMER_GROUP)
    public void consumeCreateOrderEvent(ConsumerRecord<String, PaymentRequest> consumerRecord) {
        if (consumerRecord.value() == null) {
            log.error("Failed to deserialize PaymentRequest at partition={}, offset={}",
                    consumerRecord.partition(), consumerRecord.offset());
            return;
        }

        PaymentRequest event = consumerRecord.value();
        if (paymentService.existsByOrderId(event.getOrderId())) {
            log.info("Payment already exists for order {}, skipping", event.getOrderId());
            return;
        }

        PaymentResponse paymentResponse = paymentService.save(event);

        CreatePaymentEvent createPaymentEvent = CreatePaymentEvent.builder()
                .orderId(paymentResponse.getOrderId())
                .status(paymentResponse.getStatus())
                .build();

        sendCreatePaymentEvent(createPaymentEvent);
    }

    @Override
    public void sendCreatePaymentEvent(CreatePaymentEvent event) {
        kafkaTemplate.send(KafkaConfig.PAYMENT_CREATED_TOPIC, event.getOrderId().toString(), event)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error("Failed to send CREATE_PAYMENT event for order {}: {}",
                                event.getOrderId(), exception.getMessage());
                    }
                });
    }

}

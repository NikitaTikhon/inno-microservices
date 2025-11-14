package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.config.KafkaConfig;
import com.innowise.orderservice.exception.ResourceNotFoundException;
import com.innowise.orderservice.model.OrderStatus;
import com.innowise.orderservice.model.PaymentStatus;
import com.innowise.orderservice.model.dto.CreateOrderEvent;
import com.innowise.orderservice.model.dto.CreatePaymentEvent;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.service.KafkaService;
import com.innowise.orderservice.util.ExceptionMessageGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaServiceImpl implements KafkaService {

    private final KafkaTemplate<String, CreateOrderEvent> kafkaTemplate;

    private final OrderRepository orderRepository;

    @Override
    @Transactional
    @KafkaListener(topics = KafkaConfig.PAYMENT_CREATED_TOPIC, groupId = KafkaConfig.ORDER_SERVICE_PAYMENT_CONSUMER_GROUP)
    public void consumeCreatePaymentEvent(ConsumerRecord<String, CreatePaymentEvent> consumerRecord) {
        if (consumerRecord.value() == null) {
            log.error("Failed to deserialize CreatePaymentEvent at partition={}, offset={}",
                    consumerRecord.partition(), consumerRecord.offset());
            return;
        }

        CreatePaymentEvent event = consumerRecord.value();
        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(ExceptionMessageGenerator.orderNotFound(event.getOrderId())));

        if (!OrderStatus.NEW.equals(order.getStatus())) {
            log.info("Order already processed {}, skipping", event.getOrderId());
            return;
        }

        if (PaymentStatus.SUCCESS.equals(event.getStatus())) {
            order.setStatus(OrderStatus.PREPARED);
        } else {
            order.setStatus(OrderStatus.CANCELED);
        }

        orderRepository.save(order);
    }

    @Override
    public void sendCreateOrderEvent(CreateOrderEvent event) {
        kafkaTemplate.send(KafkaConfig.ORDER_CREATED_TOPIC, event.getOrderId().toString(), event)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error(exception.getMessage());
                    }
                });
    }

}

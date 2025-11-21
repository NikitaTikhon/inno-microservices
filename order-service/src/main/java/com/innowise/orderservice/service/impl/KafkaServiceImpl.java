package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.config.KafkaConfig;
import com.innowise.orderservice.model.OrderStatus;
import com.innowise.orderservice.model.PaymentStatus;
import com.innowise.orderservice.model.dto.CreateOrderEvent;
import com.innowise.orderservice.model.dto.CreatePaymentEvent;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.service.KafkaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaServiceImpl implements KafkaService {

    private final KafkaTemplate<String, CreateOrderEvent> kafkaTemplate;

    private final OrderRepository orderRepository;

    @Override
    @KafkaListener(topics = KafkaConfig.PAYMENT_CREATED_TOPIC, groupId = KafkaConfig.ORDER_SERVICE_PAYMENT_CONSUMER_GROUP)
    public void consumeCreatePaymentEvent(ConsumerRecord<String, CreatePaymentEvent> consumerRecord) {
        CreatePaymentEvent event = consumerRecord.value();
        if (event == null) {
            log.error("Failed to deserialize CreatePaymentEvent at partition={}, offset={}",
                    consumerRecord.partition(), consumerRecord.offset());
            throw new IllegalArgumentException("Invalid CreatePaymentEvent - cannot deserialize");
        }

        Order order = findOrderOrSkip(event.getOrderId(), event.getStatus());
        if (order == null) {
            return;
        }

        if (isOrderAlreadyProcessed(order)) {
            return;
        }

        updateOrderStatus(order, event.getStatus());
        orderRepository.save(order);
    }

    private Order findOrderOrSkip(Long orderId, PaymentStatus paymentStatus) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        
        if (orderOpt.isEmpty()) {
            log.error("Order not found for payment event. OrderId: {}, PaymentStatus: {}.",
                    orderId, paymentStatus);
            return null;
        }
        
        return orderOpt.get();
    }

    private boolean isOrderAlreadyProcessed(Order order) {
        if (!OrderStatus.NEW.equals(order.getStatus())) {
            log.info("Order already processed {}, skipping", order.getId());
            return true;
        }
        return false;
    }

    private void updateOrderStatus(Order order, PaymentStatus paymentStatus) {
        if (PaymentStatus.SUCCESS.equals(paymentStatus)) {
            order.setStatus(OrderStatus.PREPARED);
        } else {
            order.setStatus(OrderStatus.CANCELED);
        }
    }

    @Override
    public void sendCreateOrderEvent(CreateOrderEvent event) {
        try {
            kafkaTemplate.send(KafkaConfig.ORDER_CREATED_TOPIC, event.getOrderId().toString(), event)
                    .get(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new KafkaException("Thread interrupted while sending CREATE_ORDER for order " + event.getOrderId(), e);
        } catch (Exception e) {
            throw new KafkaException("Failed to send CREATE_ORDER for order " + event.getOrderId(), e);
        }
    }

}

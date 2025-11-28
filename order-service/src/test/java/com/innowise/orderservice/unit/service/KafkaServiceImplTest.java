package com.innowise.orderservice.unit.service;

import com.innowise.orderservice.config.KafkaConfig;
import com.innowise.orderservice.model.OrderStatus;
import com.innowise.orderservice.model.PaymentStatus;
import com.innowise.orderservice.model.dto.CreateOrderEvent;
import com.innowise.orderservice.model.dto.CreatePaymentEvent;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.service.impl.KafkaServiceImpl;
import com.innowise.orderservice.util.EventValidator;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.innowise.orderservice.util.OrderUtil.createOrder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaServiceImplTest {

    @InjectMocks
    private KafkaServiceImpl kafkaService;

    @Mock
    private KafkaTemplate<String, CreateOrderEvent> kafkaTemplate;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private EventValidator eventValidator;

    @Test
    @DisplayName("Should update order to PREPARED when payment is successful")
    void consumeCreatePaymentEvent_ShouldUpdateOrderToPrepared_WhenPaymentSuccess() {
        Long orderId = 1L;
        CreatePaymentEvent paymentEvent = CreatePaymentEvent.builder()
                .orderId(orderId)
                .status(PaymentStatus.SUCCESS)
                .build();
        ConsumerRecord<String, CreatePaymentEvent> consumerRecord = 
            new ConsumerRecord<>(KafkaConfig.PAYMENT_CREATED_TOPIC, 0, 0L, "key", paymentEvent);

        Order order = createOrder(orderId, 1L, OrderStatus.NEW);

        doNothing().when(eventValidator).validate(paymentEvent);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        kafkaService.consumeCreatePaymentEvent(consumerRecord);

        verify(eventValidator).validate(paymentEvent);
        verify(orderRepository).findById(orderId);
        
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        
        Order capturedOrder = orderCaptor.getValue();
        assertThat(capturedOrder.getId()).isEqualTo(orderId);
        assertThat(capturedOrder.getStatus()).isEqualTo(OrderStatus.PREPARED);
    }

    @Test
    @DisplayName("Should update order to CANCELED when payment fails")
    void consumeCreatePaymentEvent_ShouldUpdateOrderToCanceled_WhenPaymentFailed() {
        Long orderId = 2L;
        CreatePaymentEvent paymentEvent = CreatePaymentEvent.builder()
                .orderId(orderId)
                .status(PaymentStatus.FAILED)
                .build();
        ConsumerRecord<String, CreatePaymentEvent> consumerRecord = 
            new ConsumerRecord<>(KafkaConfig.PAYMENT_CREATED_TOPIC, 0, 0L, "key", paymentEvent);

        Order order = createOrder(orderId, 2L, OrderStatus.NEW);

        doNothing().when(eventValidator).validate(paymentEvent);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        kafkaService.consumeCreatePaymentEvent(consumerRecord);

        verify(eventValidator).validate(paymentEvent);
        verify(orderRepository).findById(orderId);
        
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        
        Order capturedOrder = orderCaptor.getValue();
        assertThat(capturedOrder.getId()).isEqualTo(orderId);
        assertThat(capturedOrder.getStatus()).isEqualTo(OrderStatus.CANCELED);
    }

    @Test
    @DisplayName("Should skip processing when order status is not NEW")
    void consumeCreatePaymentEvent_ShouldSkipProcessing_WhenOrderStatusNotNew() {
        Long orderId = 3L;
        CreatePaymentEvent paymentEvent = CreatePaymentEvent.builder()
                .orderId(orderId)
                .status(PaymentStatus.SUCCESS)
                .build();
        ConsumerRecord<String, CreatePaymentEvent> consumerRecord = 
            new ConsumerRecord<>(KafkaConfig.PAYMENT_CREATED_TOPIC, 0, 0L, "key", paymentEvent);

        Order order = createOrder(orderId, 3L, OrderStatus.PREPARED);

        doNothing().when(eventValidator).validate(paymentEvent);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        kafkaService.consumeCreatePaymentEvent(consumerRecord);

        verify(eventValidator).validate(paymentEvent);
        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should skip processing when order not found")
    void consumeCreatePaymentEvent_ShouldSkipProcessing_WhenOrderNotFound() {
        Long orderId = 999L;
        CreatePaymentEvent paymentEvent = CreatePaymentEvent.builder()
                .orderId(orderId)
                .status(PaymentStatus.SUCCESS)
                .build();
        ConsumerRecord<String, CreatePaymentEvent> consumerRecord = 
            new ConsumerRecord<>(KafkaConfig.PAYMENT_CREATED_TOPIC, 0, 0L, "key", paymentEvent);

        doNothing().when(eventValidator).validate(paymentEvent);
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        kafkaService.consumeCreatePaymentEvent(consumerRecord);

        verify(eventValidator).validate(paymentEvent);
        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when consumer record value is null")
    void consumeCreatePaymentEvent_ShouldThrowException_WhenValueIsNull() {
        ConsumerRecord<String, CreatePaymentEvent> consumerRecord = 
            new ConsumerRecord<>(KafkaConfig.PAYMENT_CREATED_TOPIC, 0, 0L, "key", null);

        doThrow(new IllegalArgumentException("Event cannot be null - deserialization failed"))
                .when(eventValidator).validate(null);

        assertThatThrownBy(() -> kafkaService.consumeCreatePaymentEvent(consumerRecord))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Event cannot be null");

        verify(eventValidator).validate(null);
        verify(orderRepository, never()).findById(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should send create order event successfully")
    void sendCreateOrderEvent_ShouldSendEvent() {
        Long orderId = 1L;
        Long userId = 1L;
        CreateOrderEvent event = CreateOrderEvent.builder()
                .orderId(orderId)
                .userId(userId)
                .paymentAmount(BigDecimal.valueOf(1000.00))
                .build();

        SendResult<String, CreateOrderEvent> mockResult = mock(SendResult.class);
        CompletableFuture<SendResult<String, CreateOrderEvent>> future = 
            CompletableFuture.completedFuture(mockResult);

        when(kafkaTemplate.send(anyString(), anyString(), any(CreateOrderEvent.class)))
            .thenReturn(future);

        kafkaService.sendCreateOrderEvent(event);

        verify(kafkaTemplate).send(
            KafkaConfig.ORDER_CREATED_TOPIC,
            orderId.toString(),
            event
        );
    }

    @Test
    @DisplayName("Should throw KafkaException when sending event fails")
    void sendCreateOrderEvent_ShouldThrowKafkaException_WhenSendFails() {
        Long orderId = 1L;
        Long userId = 1L;
        CreateOrderEvent event = CreateOrderEvent.builder()
                .orderId(orderId)
                .userId(userId)
                .paymentAmount(BigDecimal.valueOf(1000.00))
                .build();

        CompletableFuture<SendResult<String, CreateOrderEvent>> failedFuture = 
            CompletableFuture.failedFuture(new RuntimeException("Kafka error"));

        when(kafkaTemplate.send(anyString(), anyString(), any(CreateOrderEvent.class)))
            .thenReturn(failedFuture);

        assertThatThrownBy(() -> kafkaService.sendCreateOrderEvent(event))
            .isInstanceOf(KafkaException.class)
            .hasMessageContaining("Failed to send CREATE_ORDER for order " + orderId);

        verify(kafkaTemplate).send(
            KafkaConfig.ORDER_CREATED_TOPIC,
            orderId.toString(),
            event
        );
    }

}

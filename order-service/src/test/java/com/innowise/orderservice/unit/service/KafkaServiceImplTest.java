package com.innowise.orderservice.unit.service;

import com.innowise.orderservice.config.KafkaConfig;
import com.innowise.orderservice.exception.ResourceNotFoundException;
import com.innowise.orderservice.model.OrderStatus;
import com.innowise.orderservice.model.PaymentStatus;
import com.innowise.orderservice.model.dto.CreateOrderEvent;
import com.innowise.orderservice.model.dto.CreatePaymentEvent;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.service.impl.KafkaServiceImpl;
import com.innowise.orderservice.util.ExceptionMessageGenerator;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        kafkaService.consumeCreatePaymentEvent(consumerRecord);

        verify(orderRepository).findById(orderId);
        
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        
        Order capturedOrder = orderCaptor.getValue();
        assertThat(capturedOrder.getId()).isEqualTo(orderId);
        assertThat(capturedOrder.getStatus()).isEqualTo(OrderStatus.PREPARED);
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

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        kafkaService.consumeCreatePaymentEvent(consumerRecord);

        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when order not found")
    void consumeCreatePaymentEvent_ShouldThrowException_WhenOrderNotFound() {
        Long orderId = 999L;
        CreatePaymentEvent paymentEvent = CreatePaymentEvent.builder()
                .orderId(orderId)
                .status(PaymentStatus.SUCCESS)
                .build();
        ConsumerRecord<String, CreatePaymentEvent> consumerRecord = 
            new ConsumerRecord<>(KafkaConfig.PAYMENT_CREATED_TOPIC, 0, 0L, "key", paymentEvent);

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> kafkaService.consumeCreatePaymentEvent(consumerRecord))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(ExceptionMessageGenerator.orderNotFound(orderId));

        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should log error and skip processing when consumer record value is null")
    void consumeCreatePaymentEvent_ShouldSkipProcessing_WhenValueIsNull() {
        ConsumerRecord<String, CreatePaymentEvent> consumerRecord = 
            new ConsumerRecord<>(KafkaConfig.PAYMENT_CREATED_TOPIC, 0, 0L, "key", null);

        kafkaService.consumeCreatePaymentEvent(consumerRecord);

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

        when(kafkaTemplate.send(anyString(), anyString(), any(CreateOrderEvent.class)))
            .thenReturn(CompletableFuture.completedFuture(mockResult));

        kafkaService.sendCreateOrderEvent(event);

        verify(kafkaTemplate).send(
            KafkaConfig.ORDER_CREATED_TOPIC,
            orderId.toString(),
            event
        );
    }

}

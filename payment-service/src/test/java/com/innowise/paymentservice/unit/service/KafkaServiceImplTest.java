package com.innowise.paymentservice.unit.service;

import com.innowise.paymentservice.config.KafkaConfig;
import com.innowise.paymentservice.model.PaymentStatus;
import com.innowise.paymentservice.model.dto.CreatePaymentEvent;
import com.innowise.paymentservice.model.dto.PaymentRequest;
import com.innowise.paymentservice.model.dto.PaymentResponse;
import com.innowise.paymentservice.service.OutboxEventService;
import com.innowise.paymentservice.service.PaymentService;
import com.innowise.paymentservice.service.impl.KafkaServiceImpl;
import com.innowise.paymentservice.util.EventValidator;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static com.innowise.paymentservice.util.PaymentUtil.createPaymentRequest;
import static com.innowise.paymentservice.util.PaymentUtil.createPaymentResponse;
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
    private KafkaTemplate<String, CreatePaymentEvent> kafkaTemplate;

    @Mock
    private PaymentService paymentService;

    @Mock
    private OutboxEventService outboxEventService;

    @Mock
    private EventValidator eventValidator;

    @Test
    @DisplayName("Should process order event and create payment when payment does not exist")
    void consumeCreateOrderEvent_ShouldCreatePayment_WhenPaymentDoesNotExist() {
        Long orderId = 1L;
        PaymentRequest paymentRequest = createPaymentRequest(orderId, 1L, BigDecimal.valueOf(1000.00));
        ConsumerRecord<String, PaymentRequest> consumerRecord = 
            new ConsumerRecord<>(KafkaConfig.ORDER_CREATED_TOPIC, 0, 0L, "key", paymentRequest);
        
        PaymentResponse paymentResponse = createPaymentResponse(orderId, PaymentStatus.SUCCESS);

        doNothing().when(eventValidator).validate(paymentRequest);
        when(paymentService.existsByOrderId(orderId)).thenReturn(false);
        when(paymentService.save(paymentRequest)).thenReturn(paymentResponse);

        kafkaService.consumeCreateOrderEvent(consumerRecord);

        verify(eventValidator).validate(paymentRequest);
        verify(paymentService).existsByOrderId(orderId);
        verify(paymentService).save(paymentRequest);
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any(CreatePaymentEvent.class));
    }

    @Test
    @DisplayName("Should skip processing when payment and outbox event already exist")
    void consumeCreateOrderEvent_ShouldSkipProcessing_WhenPaymentAndOutboxEventExist() {
        Long orderId = 1L;
        PaymentRequest paymentRequest = createPaymentRequest(orderId, 1L, BigDecimal.valueOf(1000.00));
        ConsumerRecord<String, PaymentRequest> consumerRecord = 
            new ConsumerRecord<>(KafkaConfig.ORDER_CREATED_TOPIC, 0, 0L, paymentRequest.getOrderId().toString(), paymentRequest);

        doNothing().when(eventValidator).validate(paymentRequest);
        when(paymentService.existsByOrderId(orderId)).thenReturn(true);
        when(outboxEventService.existsByOrderId(orderId)).thenReturn(true);

        kafkaService.consumeCreateOrderEvent(consumerRecord);

        verify(eventValidator).validate(paymentRequest);
        verify(paymentService).existsByOrderId(orderId);
        verify(outboxEventService).existsByOrderId(orderId);
        verify(paymentService, never()).save(any());
        verify(paymentService, never()).findByOrderId(any());
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any(CreatePaymentEvent.class));
    }

    @Test
    @DisplayName("Should recover missing outbox event when payment exists but event does not")
    void consumeCreateOrderEvent_ShouldRecoverOutboxEvent_WhenPaymentExistsButEventMissing() {
        Long orderId = 1L;
        PaymentRequest paymentRequest = createPaymentRequest(orderId, 1L, BigDecimal.valueOf(1000.00));
        ConsumerRecord<String, PaymentRequest> consumerRecord = 
            new ConsumerRecord<>(KafkaConfig.ORDER_CREATED_TOPIC, 0, 0L, paymentRequest.getOrderId().toString(), paymentRequest);
        
        PaymentResponse paymentResponse = createPaymentResponse(orderId, PaymentStatus.SUCCESS);

        doNothing().when(eventValidator).validate(paymentRequest);
        when(paymentService.existsByOrderId(orderId)).thenReturn(true);
        when(outboxEventService.existsByOrderId(orderId)).thenReturn(false);
        when(paymentService.findByOrderId(orderId)).thenReturn(paymentResponse);

        kafkaService.consumeCreateOrderEvent(consumerRecord);

        verify(eventValidator).validate(paymentRequest);
        verify(paymentService).existsByOrderId(orderId);
        verify(outboxEventService).existsByOrderId(orderId);
        verify(paymentService).findByOrderId(orderId);
        verify(outboxEventService).save(any(CreatePaymentEvent.class));
        verify(paymentService, never()).save(any(PaymentRequest.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when consumer record value is null")
    void consumeCreateOrderEvent_ShouldThrowException_WhenValueIsNull() {
        ConsumerRecord<String, PaymentRequest> consumerRecord =
            new ConsumerRecord<>(KafkaConfig.ORDER_CREATED_TOPIC, 0, 0L, "key", null);

        doThrow(new IllegalArgumentException("Event cannot be null - deserialization failed"))
                .when(eventValidator).validate(null);

        assertThatThrownBy(() -> kafkaService.consumeCreateOrderEvent(consumerRecord))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Event cannot be null");

        verify(eventValidator).validate(null);
        verify(paymentService, never()).existsByOrderId(any());
        verify(paymentService, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any(CreatePaymentEvent.class));
    }

    @Test
    @DisplayName("Should process order event and create payment even when payment fails")
    void consumeCreateOrderEvent_ShouldCreatePayment_WhenPaymentFails() {
        Long orderId = 2L;
        PaymentRequest paymentRequest = createPaymentRequest(orderId, 2L, BigDecimal.valueOf(500.00));
        ConsumerRecord<String, PaymentRequest> consumerRecord = 
            new ConsumerRecord<>(KafkaConfig.ORDER_CREATED_TOPIC, 0, 0L, "key", paymentRequest);
        
        PaymentResponse paymentResponse = createPaymentResponse(orderId, PaymentStatus.FAILED);

        doNothing().when(eventValidator).validate(paymentRequest);
        when(paymentService.existsByOrderId(orderId)).thenReturn(false);
        when(paymentService.save(paymentRequest)).thenReturn(paymentResponse);

        kafkaService.consumeCreateOrderEvent(consumerRecord);

        verify(eventValidator).validate(paymentRequest);
        verify(paymentService).existsByOrderId(orderId);
        verify(paymentService).save(paymentRequest);
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any(CreatePaymentEvent.class));
    }

    @Test
    @DisplayName("Should send create payment event successfully")
    void sendCreatePaymentEvent_ShouldSendEvent() {
        Long orderId = 1L;
        CreatePaymentEvent event = CreatePaymentEvent.builder()
                .orderId(orderId)
                .status(PaymentStatus.SUCCESS)
                .build();

        SendResult<String, CreatePaymentEvent> mockResult = mock(SendResult.class);
        CompletableFuture<SendResult<String, CreatePaymentEvent>> future = 
            CompletableFuture.completedFuture(mockResult);

        when(kafkaTemplate.send(anyString(), anyString(), any(CreatePaymentEvent.class)))
            .thenReturn(future);

        kafkaService.sendCreatePaymentEvent(event);

        verify(kafkaTemplate).send(
            KafkaConfig.PAYMENT_CREATED_TOPIC,
            orderId.toString(),
            event
        );
    }

    @Test
    @DisplayName("Should throw KafkaException when sending event fails")
    void sendCreatePaymentEvent_ShouldThrowKafkaException_WhenSendFails() {
        Long orderId = 1L;
        CreatePaymentEvent event = CreatePaymentEvent.builder()
                .orderId(orderId)
                .status(PaymentStatus.SUCCESS)
                .build();

        CompletableFuture<SendResult<String, CreatePaymentEvent>> failedFuture = 
            CompletableFuture.failedFuture(new RuntimeException("Kafka error"));

        when(kafkaTemplate.send(anyString(), anyString(), any(CreatePaymentEvent.class)))
            .thenReturn(failedFuture);

        assertThatThrownBy(() -> kafkaService.sendCreatePaymentEvent(event))
            .isInstanceOf(KafkaException.class)
            .hasMessageContaining("Failed to send CREATE_PAYMENT event");

        verify(kafkaTemplate).send(
            KafkaConfig.PAYMENT_CREATED_TOPIC,
            orderId.toString(),
            event
        );
    }

}


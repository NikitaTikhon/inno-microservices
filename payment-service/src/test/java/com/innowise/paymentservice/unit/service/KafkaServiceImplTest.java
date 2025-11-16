package com.innowise.paymentservice.unit.service;

import com.innowise.paymentservice.config.KafkaConfig;
import com.innowise.paymentservice.model.PaymentStatus;
import com.innowise.paymentservice.model.dto.CreatePaymentEvent;
import com.innowise.paymentservice.model.dto.PaymentRequest;
import com.innowise.paymentservice.model.dto.PaymentResponse;
import com.innowise.paymentservice.service.PaymentService;
import com.innowise.paymentservice.service.impl.KafkaServiceImpl;
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
import java.util.concurrent.CompletableFuture;

import static com.innowise.paymentservice.util.PaymentUtil.createPaymentRequest;
import static com.innowise.paymentservice.util.PaymentUtil.createPaymentResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

    @Test
    @DisplayName("Should process order event and create payment when payment does not exist")
    void consumeCreateOrderEvent_ShouldCreatePayment_WhenPaymentDoesNotExist() {
        Long orderId = 1L;
        PaymentRequest paymentRequest = createPaymentRequest(orderId, 1L, BigDecimal.valueOf(1000.00));
        ConsumerRecord<String, PaymentRequest> consumerRecord = 
            new ConsumerRecord<>(KafkaConfig.ORDER_CREATED_TOPIC, 0, 0L, "key", paymentRequest);
        
        PaymentResponse paymentResponse = createPaymentResponse(orderId, PaymentStatus.SUCCESS);

        SendResult<String, CreatePaymentEvent> mockResult = mock(SendResult.class);

        when(paymentService.existsByOrderId(orderId)).thenReturn(false);
        when(paymentService.save(paymentRequest)).thenReturn(paymentResponse);
        when(kafkaTemplate.send(anyString(), anyString(), any(CreatePaymentEvent.class)))
            .thenReturn(CompletableFuture.completedFuture(mockResult));

        kafkaService.consumeCreateOrderEvent(consumerRecord);

        verify(paymentService).existsByOrderId(orderId);
        verify(paymentService).save(paymentRequest);
        
        ArgumentCaptor<CreatePaymentEvent> eventCaptor = ArgumentCaptor.forClass(CreatePaymentEvent.class);
        verify(kafkaTemplate).send(eq(KafkaConfig.PAYMENT_CREATED_TOPIC), eq(orderId.toString()), eventCaptor.capture());
        
        CreatePaymentEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getOrderId()).isEqualTo(orderId);
        assertThat(capturedEvent.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }

    @Test
    @DisplayName("Should skip processing when payment already exists for order")
    void consumeCreateOrderEvent_ShouldSkipProcessing_WhenPaymentAlreadyExists() {
        Long orderId = 1L;
        PaymentRequest paymentRequest = createPaymentRequest(orderId, 1L, BigDecimal.valueOf(1000.00));
        ConsumerRecord<String, PaymentRequest> consumerRecord = 
            new ConsumerRecord<>(KafkaConfig.ORDER_CREATED_TOPIC, 0, 0L, paymentRequest.getOrderId().toString(), paymentRequest);

        when(paymentService.existsByOrderId(orderId)).thenReturn(true);

        kafkaService.consumeCreateOrderEvent(consumerRecord);

        verify(paymentService).existsByOrderId(orderId);
        verify(paymentService, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any(CreatePaymentEvent.class));
    }

    @Test
    @DisplayName("Should log error and skip processing when consumer record value is null")
    void consumeCreateOrderEvent_ShouldSkipProcessing_WhenValueIsNull() {
        ConsumerRecord<String, PaymentRequest> consumerRecord =
            new ConsumerRecord<>(KafkaConfig.ORDER_CREATED_TOPIC, 0, 0L, "key", null);

        kafkaService.consumeCreateOrderEvent(consumerRecord);

        verify(paymentService, never()).existsByOrderId(any());
        verify(paymentService, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any(CreatePaymentEvent.class));
    }

    @Test
    @DisplayName("Should process order event and send failed payment event when payment fails")
    void consumeCreateOrderEvent_ShouldSendFailedEvent_WhenPaymentFails() {
        Long orderId = 2L;
        PaymentRequest paymentRequest = createPaymentRequest(orderId, 2L, BigDecimal.valueOf(500.00));
        ConsumerRecord<String, PaymentRequest> consumerRecord = 
            new ConsumerRecord<>(KafkaConfig.ORDER_CREATED_TOPIC, 0, 0L, "key", paymentRequest);
        
        PaymentResponse paymentResponse = createPaymentResponse(orderId, PaymentStatus.FAILED);

        SendResult<String, CreatePaymentEvent> mockResult = mock(SendResult.class);

        when(paymentService.existsByOrderId(orderId)).thenReturn(false);
        when(paymentService.save(paymentRequest)).thenReturn(paymentResponse);
        when(kafkaTemplate.send(anyString(), anyString(), any(CreatePaymentEvent.class)))
            .thenReturn(CompletableFuture.completedFuture(mockResult));

        kafkaService.consumeCreateOrderEvent(consumerRecord);

        verify(paymentService).existsByOrderId(orderId);
        verify(paymentService).save(paymentRequest);
        
        ArgumentCaptor<CreatePaymentEvent> eventCaptor = ArgumentCaptor.forClass(CreatePaymentEvent.class);
        verify(kafkaTemplate).send(eq(KafkaConfig.PAYMENT_CREATED_TOPIC), eq(orderId.toString()), eventCaptor.capture());
        
        CreatePaymentEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getOrderId()).isEqualTo(orderId);
        assertThat(capturedEvent.getStatus()).isEqualTo(PaymentStatus.FAILED);
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

        when(kafkaTemplate.send(anyString(), anyString(), any(CreatePaymentEvent.class)))
            .thenReturn(CompletableFuture.completedFuture(mockResult));

        kafkaService.sendCreatePaymentEvent(event);

        verify(kafkaTemplate).send(
            KafkaConfig.PAYMENT_CREATED_TOPIC,
            orderId.toString(),
            event
        );
    }

}


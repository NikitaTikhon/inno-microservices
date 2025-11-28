package com.innowise.paymentservice.unit.service;

import com.innowise.paymentservice.exception.ResourceNotFoundException;
import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.model.PaymentStatus;
import com.innowise.paymentservice.model.document.Payment;
import com.innowise.paymentservice.model.dto.CreatePaymentEvent;
import com.innowise.paymentservice.model.dto.PaymentRequest;
import com.innowise.paymentservice.model.dto.PaymentResponse;
import com.innowise.paymentservice.model.projection.TotalAmountProjection;
import com.innowise.paymentservice.repository.PaymentRepository;
import com.innowise.paymentservice.service.impl.OutboxEventServiceImpl;
import com.innowise.paymentservice.service.impl.PaymentProcessorServiceImpl;
import com.innowise.paymentservice.service.impl.PaymentServiceImpl;
import com.innowise.paymentservice.util.ExceptionMessageGenerator;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.innowise.paymentservice.util.PaymentUtil.createPayment;
import static com.innowise.paymentservice.util.PaymentUtil.createPaymentRequest;
import static com.innowise.paymentservice.util.PaymentUtil.createPaymentResponse;
import static com.innowise.paymentservice.util.PaymentUtil.createPaymentResponses;
import static com.innowise.paymentservice.util.PaymentUtil.createPayments;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Mock
    private PaymentProcessorServiceImpl paymentProcessorService;

    @Mock
    private OutboxEventServiceImpl outboxEventService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Test
    @DisplayName("Should create payment with SUCCESS status when random number is even")
    void save_ShouldCreatePaymentWithSuccess_WhenRandomNumberIsEven() {
        PaymentRequest paymentRequest = createPaymentRequest();
        ObjectId paymentId = new ObjectId();
        Payment savedPayment = createPayment(paymentId, paymentRequest.getOrderId(), PaymentStatus.SUCCESS);
        PaymentResponse expectedResponse = createPaymentResponse(paymentId.toString(), 
                paymentRequest.getOrderId(), PaymentStatus.SUCCESS);

        when(paymentProcessorService.processPayment()).thenReturn(PaymentStatus.SUCCESS);
        when(paymentMapper.paymentRequestToPayment(paymentRequest)).thenReturn(new Payment());
        doNothing().when(outboxEventService).save(any(CreatePaymentEvent.class));
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        when(paymentMapper.paymentToPaymentResponse(savedPayment)).thenReturn(expectedResponse);

        PaymentResponse actualResponse = paymentService.save(paymentRequest);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getId()).isEqualTo(expectedResponse.getId());
        assertThat(actualResponse.getOrderId()).isEqualTo(paymentRequest.getOrderId());
        assertThat(actualResponse.getStatus()).isEqualTo(PaymentStatus.SUCCESS);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        
        Payment capturedPayment = paymentCaptor.getValue();
        assertThat(capturedPayment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);

        verify(paymentProcessorService).processPayment();
        verify(paymentMapper).paymentRequestToPayment(paymentRequest);
        verify(paymentMapper).paymentToPaymentResponse(savedPayment);
    }

    @Test
    @DisplayName("Should return payment by orderId successfully")
    void findByOrderId_ShouldReturnPayment_WhenPaymentExists() {
        Long orderId = 1L;
        ObjectId paymentId = new ObjectId();
        Payment payment = createPayment(paymentId, orderId, PaymentStatus.SUCCESS);
        PaymentResponse expectedResponse = createPaymentResponse(paymentId.toString(), orderId, PaymentStatus.SUCCESS);

        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(payment));
        when(paymentMapper.paymentToPaymentResponse(payment)).thenReturn(expectedResponse);

        PaymentResponse actualResponse = paymentService.findByOrderId(orderId);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getId()).isEqualTo(expectedResponse.getId());
        assertThat(actualResponse.getOrderId()).isEqualTo(orderId);
        assertThat(actualResponse.getStatus()).isEqualTo(PaymentStatus.SUCCESS);

        verify(paymentRepository).findByOrderId(orderId);
        verify(paymentMapper).paymentToPaymentResponse(payment);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when payment not found by orderId")
    void findByOrderId_ShouldThrowException_WhenPaymentNotFound() {
        Long orderId = 999L;

        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.findByOrderId(orderId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(ExceptionMessageGenerator.paymentNotFound("orderId", String.valueOf(orderId)));

        verify(paymentRepository).findByOrderId(orderId);
        verify(paymentMapper, never()).paymentToPaymentResponse(any());
    }

    @Test
    @DisplayName("Should return payments by userId successfully")
    void findByUserId_ShouldReturnPayments() {
        Long userId = 1L;
        List<Payment> payments = createPayments();
        List<PaymentResponse> expectedResponses = createPaymentResponses();

        when(paymentRepository.findByUserId(userId)).thenReturn(payments);
        when(paymentMapper.paymentsToPaymentsResponse(payments)).thenReturn(expectedResponses);

        List<PaymentResponse> actualResponses = paymentService.findByUserId(userId);

        assertThat(actualResponses).isNotNull()
                .hasSize(3)
                .isEqualTo(expectedResponses);

        verify(paymentRepository).findByUserId(userId);
        verify(paymentMapper).paymentsToPaymentsResponse(payments);
    }

    @Test
    @DisplayName("Should return empty list when no payments found for userId")
    void findByUserId_ShouldReturnEmptyList_WhenNoPaymentsFound() {
        Long userId = 999L;

        when(paymentRepository.findByUserId(userId)).thenReturn(List.of());
        when(paymentMapper.paymentsToPaymentsResponse(List.of())).thenReturn(List.of());

        List<PaymentResponse> actualResponses = paymentService.findByUserId(userId);

        assertThat(actualResponses).isNotNull()
                .isEmpty();

        verify(paymentRepository).findByUserId(userId);
        verify(paymentMapper).paymentsToPaymentsResponse(List.of());
    }

    @Test
    @DisplayName("Should return payments by statuses successfully")
    void findByStatuses_ShouldReturnPayments() {
        List<PaymentStatus> statuses = List.of(PaymentStatus.SUCCESS, PaymentStatus.FAILED);
        List<Payment> payments = createPayments();
        List<PaymentResponse> expectedResponses = createPaymentResponses();

        when(paymentRepository.findByStatusIn(statuses)).thenReturn(payments);
        when(paymentMapper.paymentsToPaymentsResponse(payments)).thenReturn(expectedResponses);

        List<PaymentResponse> actualResponses = paymentService.findByStatuses(statuses);

        assertThat(actualResponses).isNotNull()
                .hasSize(3)
                .isEqualTo(expectedResponses);

        verify(paymentRepository).findByStatusIn(statuses);
        verify(paymentMapper).paymentsToPaymentsResponse(payments);
    }

    @Test
    @DisplayName("Should return empty list when no payments match statuses")
    void findByStatuses_ShouldReturnEmptyList_WhenNoPaymentsFound() {
        List<PaymentStatus> statuses = List.of(PaymentStatus.SUCCESS);

        when(paymentRepository.findByStatusIn(statuses)).thenReturn(List.of());
        when(paymentMapper.paymentsToPaymentsResponse(List.of())).thenReturn(List.of());

        List<PaymentResponse> actualResponses = paymentService.findByStatuses(statuses);

        assertThat(actualResponses).isNotNull()
                .isEmpty();

        verify(paymentRepository).findByStatusIn(statuses);
        verify(paymentMapper).paymentsToPaymentsResponse(List.of());
    }

    @Test
    @DisplayName("Should calculate total amount for date range successfully")
    void findTotalAmount_ShouldReturnTotalAmount_WhenPaymentsExist() {
        LocalDateTime from = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 12, 31, 23, 59);
        BigDecimal expectedTotal = BigDecimal.valueOf(5000.00);
        
        TotalAmountProjection projection = mock(TotalAmountProjection.class);
        when(projection.getTotal()).thenReturn(new Decimal128(expectedTotal));
        when(paymentRepository.findTotalAmount(from, to)).thenReturn(Optional.of(projection));

        BigDecimal actualTotal = paymentService.findTotalAmount(from, to);

        assertThat(actualTotal).isNotNull()
                .isEqualTo(expectedTotal);

        verify(paymentRepository).findTotalAmount(from, to);
    }

    @Test
    @DisplayName("Should return zero when no payments in date range")
    void findTotalAmount_ShouldReturnZero_WhenNoPaymentsFound() {
        LocalDateTime from = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 12, 31, 23, 59);

        when(paymentRepository.findTotalAmount(from, to)).thenReturn(Optional.empty());

        BigDecimal actualTotal = paymentService.findTotalAmount(from, to);

        assertThat(actualTotal).isNotNull()
                .isEqualTo(BigDecimal.ZERO);

        verify(paymentRepository).findTotalAmount(from, to);
    }

    @Test
    @DisplayName("Should return true when payment exists by orderId")
    void existsByOrderId_ShouldReturnTrue_WhenPaymentExists() {
        Long orderId = 1L;

        when(paymentRepository.existsByOrderId(orderId)).thenReturn(true);

        boolean exists = paymentService.existsByOrderId(orderId);

        assertThat(exists).isTrue();

        verify(paymentRepository).existsByOrderId(orderId);
    }

    @Test
    @DisplayName("Should return false when payment does not exist by orderId")
    void existsByOrderId_ShouldReturnFalse_WhenPaymentNotExists() {
        Long orderId = 999L;

        when(paymentRepository.existsByOrderId(orderId)).thenReturn(false);

        boolean exists = paymentService.existsByOrderId(orderId);

        assertThat(exists).isFalse();

        verify(paymentRepository).existsByOrderId(orderId);
    }

}




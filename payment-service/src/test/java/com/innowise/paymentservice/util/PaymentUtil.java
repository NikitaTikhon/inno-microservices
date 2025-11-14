package com.innowise.paymentservice.util;

import com.innowise.paymentservice.model.PaymentStatus;
import com.innowise.paymentservice.model.document.Payment;
import com.innowise.paymentservice.model.dto.PaymentRequest;
import com.innowise.paymentservice.model.dto.PaymentResponse;
import org.bson.types.ObjectId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class PaymentUtil {

    private PaymentUtil() {}

    public static PaymentRequest createPaymentRequest(Long orderId, Long userId, BigDecimal amount) {
        return PaymentRequest.builder()
                .orderId(orderId)
                .userId(userId)
                .paymentAmount(amount)
                .build();
    }

    public static PaymentRequest createPaymentRequest() {
        return createPaymentRequest(1L, 1L, BigDecimal.valueOf(1000.00));
    }

    public static Payment createPayment(ObjectId id, Long orderId, Long userId, BigDecimal amount, PaymentStatus status, LocalDateTime timestamp) {
        return Payment.builder()
                .id(id)
                .orderId(orderId)
                .userId(userId)
                .paymentAmount(amount)
                .status(status)
                .timestamp(timestamp)
                .build();
    }

    public static Payment createPayment(ObjectId id, Long orderId, Long userId, BigDecimal amount, PaymentStatus status) {
        return createPayment(id, orderId, userId, amount, status, LocalDateTime.now());
    }

    public static Payment createPayment(Long orderId, Long userId, BigDecimal amount, PaymentStatus status, LocalDateTime timestamp) {
        return createPayment(new ObjectId(), orderId, userId, amount, status, timestamp);
    }

    public static Payment createPayment(Long orderId, Long userId, PaymentStatus status) {
        return createPayment(new ObjectId(), orderId, userId, BigDecimal.valueOf(1000.00), status);
    }

    public static Payment createPayment(ObjectId id, Long orderId, PaymentStatus status) {
        return createPayment(id, orderId, 1L, BigDecimal.valueOf(1000.00), status);
    }

    public static PaymentResponse createPaymentResponse(String id, Long orderId, Long userId, BigDecimal amount, PaymentStatus status) {
        return PaymentResponse.builder()
                .id(id)
                .orderId(orderId)
                .userId(userId)
                .paymentAmount(amount)
                .status(status)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static PaymentResponse createPaymentResponse(String id, Long orderId, PaymentStatus status) {
        return createPaymentResponse(id, orderId, 1L, BigDecimal.valueOf(1000.00), status);
    }

    public static PaymentResponse createPaymentResponse(Long orderId, PaymentStatus status) {
        return createPaymentResponse(new ObjectId().toString(), orderId, status);
    }

    public static List<Payment> createPayments() {
        return List.of(
                createPayment(1L, 1L, PaymentStatus.SUCCESS),
                createPayment(2L, 2L, PaymentStatus.FAILED),
                createPayment(3L, 3L, PaymentStatus.SUCCESS)
        );
    }

    public static List<PaymentResponse> createPaymentResponses() {
        return List.of(
                createPaymentResponse(1L, PaymentStatus.SUCCESS),
                createPaymentResponse(2L, PaymentStatus.FAILED),
                createPaymentResponse(3L, PaymentStatus.SUCCESS)
        );
    }

}


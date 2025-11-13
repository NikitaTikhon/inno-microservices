package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.exception.ResourceNotFoundException;
import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.model.PaymentStatus;
import com.innowise.paymentservice.model.document.Payment;
import com.innowise.paymentservice.model.dto.PaymentRequest;
import com.innowise.paymentservice.model.dto.PaymentResponse;
import com.innowise.paymentservice.model.projection.TotalAmountProjection;
import com.innowise.paymentservice.repository.PaymentRepository;
import com.innowise.paymentservice.service.PaymentService;
import com.innowise.paymentservice.util.ExceptionMessageGenerator;
import lombok.RequiredArgsConstructor;
import org.bson.types.Decimal128;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final RandomNumberRestClientImpl randomNumberRestClient;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional
    public PaymentResponse save(PaymentRequest paymentRequest) {
        PaymentStatus paymentStatus = randomNumberRestClient.getRandomNumber() % 2 == 0
                ? PaymentStatus.SUCCESS
                : PaymentStatus.FAILED;

        Payment payment = paymentMapper.paymentRequestToPayment(paymentRequest);
        payment.setStatus(paymentStatus);

        return paymentMapper.paymentToPaymentResponse(paymentRepository.save(payment));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse findByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(ExceptionMessageGenerator.paymentNotFound(orderId)));

        return paymentMapper.paymentToPaymentResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> findByUserId(Long userId) {
        List<Payment> payments = paymentRepository.findByUserId(userId);

        return paymentMapper.paymentsToPaymentsResponse(payments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> findByStatuses(List<PaymentStatus> statuses) {
        List<Payment> payments = paymentRepository.findByStatusIn(statuses);

        return paymentMapper.paymentsToPaymentsResponse(payments);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal findTotalAmount(LocalDateTime from, LocalDateTime to) {
        Optional<TotalAmountProjection> totalAmountOpt = paymentRepository.findTotalAmount(from, to);

        return totalAmountOpt.map(TotalAmountProjection::getTotal)
                .map(Decimal128::bigDecimalValue)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean existsByOrderId(Long orderId) {
        return paymentRepository.existsByOrderId(orderId);
    }

}

package com.innowise.paymentservice.integration.repository;

import com.innowise.paymentservice.integration.BaseIntegrationRepositoryTest;
import com.innowise.paymentservice.model.PaymentStatus;
import com.innowise.paymentservice.model.document.Payment;
import com.innowise.paymentservice.model.projection.TotalAmountProjection;
import com.innowise.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.innowise.paymentservice.util.PaymentUtil.createPayment;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentRepositoryIT extends BaseIntegrationRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
    }

    @Test
    @DisplayName("Should save payment successfully")
    void save_ShouldSavePayment() {
        Payment payment = createPayment(1L, 1L, PaymentStatus.SUCCESS);

        Payment savedPayment = paymentRepository.save(payment);

        assertThat(savedPayment).isNotNull();
        assertThat(savedPayment.getId()).isNotNull();
        assertThat(savedPayment.getOrderId()).isEqualTo(1L);
        assertThat(savedPayment.getUserId()).isEqualTo(1L);
        assertThat(savedPayment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(savedPayment.getPaymentAmount()).isEqualTo(BigDecimal.valueOf(1000.00));
    }

    @Test
    @DisplayName("Should find payment by orderId")
    void findByOrderId_ShouldReturnPayment_WhenExists() {
        Long orderId = 1L;
        Payment payment = createPayment(orderId, 1L, PaymentStatus.SUCCESS);
        paymentRepository.save(payment);

        Optional<Payment> found = paymentRepository.findByOrderId(orderId);

        assertThat(found).isPresent();
        assertThat(found.get().getOrderId()).isEqualTo(orderId);
        assertThat(found.get().getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }

    @Test
    @DisplayName("Should return empty when payment not found by orderId")
    void findByOrderId_ShouldReturnEmpty_WhenNotExists() {
        Optional<Payment> found = paymentRepository.findByOrderId(999L);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find all payments by userId")
    void findByUserId_ShouldReturnPayments() {
        Long userId = 1L;
        Payment payment1 = createPayment(1L, userId, PaymentStatus.SUCCESS);
        Payment payment2 = createPayment(2L, userId, PaymentStatus.FAILED);
        Payment payment3 = createPayment(3L, 2L, PaymentStatus.SUCCESS);
        
        paymentRepository.saveAll(List.of(payment1, payment2, payment3));

        List<Payment> payments = paymentRepository.findByUserId(userId);

        assertThat(payments).hasSize(2);
        assertThat(payments).extracting(Payment::getUserId)
                .containsOnly(userId);
        assertThat(payments).extracting(Payment::getOrderId)
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("Should return empty list when no payments for userId")
    void findByUserId_ShouldReturnEmptyList_WhenNoPayments() {
        List<Payment> payments = paymentRepository.findByUserId(999L);

        assertThat(payments).isEmpty();
    }

    @Test
    @DisplayName("Should find payments by statuses")
    void findByStatusIn_ShouldReturnMatchingPayments() {
        Payment payment1 = createPayment(1L, 1L, PaymentStatus.SUCCESS);
        Payment payment2 = createPayment(2L, 1L, PaymentStatus.FAILED);
        Payment payment3 = createPayment(3L, 2L, PaymentStatus.SUCCESS);
        
        paymentRepository.saveAll(List.of(payment1, payment2, payment3));

        List<Payment> successPayments = paymentRepository.findByStatusIn(List.of(PaymentStatus.SUCCESS));

        assertThat(successPayments).hasSize(2);
        assertThat(successPayments).extracting(Payment::getStatus)
                .containsOnly(PaymentStatus.SUCCESS);
        assertThat(successPayments).extracting(Payment::getOrderId)
                .containsExactlyInAnyOrder(1L, 3L);
    }

    @Test
    @DisplayName("Should calculate total amount for date range")
    void findTotalAmount_ShouldReturnTotal_WhenPaymentsExist() {
        LocalDateTime now = LocalDateTime.now();
        
        Payment payment1 = createPayment(1L, 1L, BigDecimal.valueOf(100.00), 
                PaymentStatus.SUCCESS, now.minusDays(2));
        Payment payment2 = createPayment(2L, 1L, BigDecimal.valueOf(200.00), 
                PaymentStatus.SUCCESS, now.minusDays(1));
        Payment payment3 = createPayment(3L, 1L, BigDecimal.valueOf(300.00), 
                PaymentStatus.SUCCESS, now.plusDays(1));
        
        paymentRepository.saveAll(List.of(payment1, payment2, payment3));

        LocalDateTime from = now.minusDays(3);
        LocalDateTime to = now;
        Optional<TotalAmountProjection> result = paymentRepository.findTotalAmount(from, to);

        assertThat(result).isPresent();
        assertThat(result.get().getTotal().bigDecimalValue())
                .isEqualByComparingTo(BigDecimal.valueOf(300.00));
    }

    @Test
    @DisplayName("Should return empty when no payments in date range")
    void findTotalAmount_ShouldReturnEmpty_WhenNoPaymentsInRange() {
        Payment payment = createPayment(1L, 1L, PaymentStatus.SUCCESS);
        paymentRepository.save(payment);

        LocalDateTime from = LocalDateTime.now().minusYears(2);
        LocalDateTime to = LocalDateTime.now().minusYears(1);
        Optional<TotalAmountProjection> result = paymentRepository.findTotalAmount(from, to);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return true when payment exists by orderId")
    void existsByOrderId_ShouldReturnTrue_WhenExists() {
        Long orderId = 1L;
        Payment payment = createPayment(orderId, 1L, PaymentStatus.SUCCESS);
        paymentRepository.save(payment);

        boolean exists = paymentRepository.existsByOrderId(orderId);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when payment does not exist by orderId")
    void existsByOrderId_ShouldReturnFalse_WhenNotExists() {
        boolean exists = paymentRepository.existsByOrderId(999L);

        assertThat(exists).isFalse();
    }

}

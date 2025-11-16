package com.innowise.paymentservice.unit.service;

import com.innowise.paymentservice.model.PaymentStatus;
import com.innowise.paymentservice.service.RandomNumberRestClient;
import com.innowise.paymentservice.service.impl.PaymentProcessorServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentProcessorServiceTest {

    @InjectMocks
    private PaymentProcessorServiceImpl paymentProcessorService;

    @Mock
    private RandomNumberRestClient randomNumberRestClient;

    @Test
    @DisplayName("Should return SUCCESS when random number is even")
    void processPayment_ShouldReturnSuccess_WhenNumberIsEven() {
        when(randomNumberRestClient.getRandomNumber()).thenReturn(2L);

        PaymentStatus paymentStatus = paymentProcessorService.processPayment();

        assertThat(paymentStatus).isEqualTo(PaymentStatus.SUCCESS);
        verify(randomNumberRestClient).getRandomNumber();
    }

    @Test
    @DisplayName("Should return FAILED when random number is odd")
    void processPayment_ShouldReturnFailed_WhenNumberIsOdd() {
        when(randomNumberRestClient.getRandomNumber()).thenReturn(3L);

        PaymentStatus paymentStatus = paymentProcessorService.processPayment();

        assertThat(paymentStatus).isEqualTo(PaymentStatus.FAILED);
        verify(randomNumberRestClient).getRandomNumber();
    }

}


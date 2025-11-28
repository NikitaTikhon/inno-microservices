package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.model.PaymentStatus;
import com.innowise.paymentservice.service.PaymentProcessorService;
import com.innowise.paymentservice.service.RandomNumberRestClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentProcessorServiceImpl implements PaymentProcessorService {

    private final RandomNumberRestClient randomNumberRestClient;

    @Override
    public PaymentStatus processPayment() {
        Long number = randomNumberRestClient.getRandomNumber();

        return number % 2 == 0 ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
    }

}

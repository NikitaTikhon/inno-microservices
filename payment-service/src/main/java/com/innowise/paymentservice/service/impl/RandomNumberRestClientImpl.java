package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.exception.ExternalServiceException;
import com.innowise.paymentservice.exception.ResourceNotFoundException;
import com.innowise.paymentservice.service.RandomNumberRestClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class RandomNumberRestClientImpl implements RandomNumberRestClient {

    private final RestClient randomNumberRestClient;

    public RandomNumberRestClientImpl(@Qualifier("randomNumberRestClient") RestClient randomNumberRestClient) {
        this.randomNumberRestClient = randomNumberRestClient;
    }

    @Override
    @CircuitBreaker(name = "random-number-api")
    public Long getRandomNumber() {
        List<Long> numbers = randomNumberRestClient.get()
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        (request, response) -> {
                            throw new ExternalServiceException("Client error from random number API");
                        })
                .onStatus(HttpStatusCode::is5xxServerError,
                        (request, response) -> {
                            throw new ExternalServiceException("Server error from random number API");
                        })
                .body(new ParameterizedTypeReference<>() {});

        if (numbers == null || numbers.isEmpty()) {
            throw new ResourceNotFoundException("Random number not found");
        }

        return numbers.getFirst();
    }

}

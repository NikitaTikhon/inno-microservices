package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.exception.ExternalServiceException;
import com.innowise.paymentservice.exception.ResourceNotFoundException;
import com.innowise.paymentservice.service.RandomNumberRestClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Service
public class RandomNumberRestClientImpl implements RandomNumberRestClient {

    @Value("${services.random-number-api.fallback-value:2}")
    private Long fallbackValue;

    private final RestClient randomNumberRestClient;

    public RandomNumberRestClientImpl(@Qualifier("randomNumberRestClient") RestClient randomNumberRestClient) {
        this.randomNumberRestClient = randomNumberRestClient;
    }

    @Override
    @CircuitBreaker(name = "random-number-api", fallbackMethod = "getRandomNumberFallback")
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

    private Long getRandomNumberFallback(Throwable throwable) {
        log.error("Random number API unavailable, using fallback. Reason: {}", throwable.getMessage());
        return fallbackValue;
    }

}

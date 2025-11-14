package com.innowise.paymentservice.integration.service;


import com.innowise.paymentservice.exception.ExternalServiceException;
import com.innowise.paymentservice.exception.ResourceNotFoundException;
import com.innowise.paymentservice.integration.BaseIntegrationTest;
import com.innowise.paymentservice.service.RandomNumberRestClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RandomNumberRestClientIT extends BaseIntegrationTest {

    @Autowired
    private RandomNumberRestClient randomNumberRestClient;

    @Test
    @DisplayName("Should return first random number from API")
    void getRandomNumber_ShouldReturnFirstNumber_WhenApiReturnsValidResponse() {
        stubRandomNumbers(42L, 13L, 99L);

        Long randomNumber = randomNumberRestClient.getRandomNumber();

        assertThat(randomNumber).isEqualTo(42L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when API returns empty list")
    void getRandomNumber_ShouldThrowException_WhenApiReturnsEmptyList() {
        stubRandomNumbers();

        assertThatThrownBy(() -> randomNumberRestClient.getRandomNumber())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Random number not found");
    }

    @Test
    @DisplayName("Should throw ExternalServiceException when API returns 5xx error")
    void getRandomNumber_ShouldThrowException_When5xxError() {
        stubFor(get(urlEqualTo("/"))
                .willReturn(aResponse()
                        .withStatus(500)));

        assertThatThrownBy(() -> randomNumberRestClient.getRandomNumber())
                .isInstanceOf(ExternalServiceException.class)
                .hasMessageContaining("Server error from random number API");
    }

    private void stubRandomNumbers(Long... randomNumbers) {
        String json = Arrays.toString(randomNumbers);

        stubFor(get(urlEqualTo("/"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(json)));
    }

}


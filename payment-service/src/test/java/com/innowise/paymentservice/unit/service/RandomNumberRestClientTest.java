package com.innowise.paymentservice.unit.service;

import com.innowise.paymentservice.exception.ResourceNotFoundException;
import com.innowise.paymentservice.service.impl.RandomNumberRestClientImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RandomNumberRestClientTest {

    @InjectMocks
    private RandomNumberRestClientImpl randomNumberRestClient;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Test
    @DisplayName("Should return random number successfully")
    void getRandomNumber_ShouldReturnNumber_WhenApiReturnsValidResponse() {
        List<Long> expectedNumbers = List.of(42L, 13L, 99L);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(expectedNumbers);

        Long actualNumber = randomNumberRestClient.getRandomNumber();

        assertThat(actualNumber).isNotNull()
                .isEqualTo(42L);

        verify(restClient).get();
        verify(requestHeadersUriSpec).retrieve();
        verify(responseSpec).body(any(ParameterizedTypeReference.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when API returns empty list")
    void getRandomNumber_ShouldThrowException_WhenApiReturnsEmptyList() {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(List.of());

        assertThatThrownBy(() -> randomNumberRestClient.getRandomNumber())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Random number not found");

        verify(restClient).get();
        verify(requestHeadersUriSpec).retrieve();
        verify(responseSpec).body(any(ParameterizedTypeReference.class));
    }

}


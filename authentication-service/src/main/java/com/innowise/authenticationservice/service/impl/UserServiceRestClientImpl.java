package com.innowise.authenticationservice.service.impl;

import com.innowise.authenticationservice.model.dto.UserDto;
import com.innowise.authenticationservice.service.UserServiceRestClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static com.innowise.authenticationservice.config.constant.SecurityConstant.INTERNAL_SERVICE_API_KEY_HEADER;

@Service
public class UserServiceRestClientImpl implements UserServiceRestClient {

    @Value("${security.internal.api.key}")
    private String internalApiKey;

    @Value("${services.user-service.uri}")
    private String userServiceUrl;

    private final RestTemplate userServiceRestClient;

    public UserServiceRestClientImpl(@Qualifier("userServiceRestTemplate") RestTemplate userServiceRestClient) {
        this.userServiceRestClient = userServiceRestClient;
    }

    @Override
    @CircuitBreaker(name = "user-service")
    public UserDto save(UserDto userDto) {
        HttpHeaders headers = headersWithInternalApiKey();
        HttpEntity<UserDto> request = new HttpEntity<>(userDto, headers);

        ResponseEntity<UserDto> response = userServiceRestClient.exchange(
                userServiceUrl + "/api/v1/users",
                HttpMethod.POST,
                request,
                UserDto.class
        );

        return response.getBody();
    }

    @Override
    @CircuitBreaker(name = "user-service")
    public void deleteById(Long id) {
        HttpHeaders headers = headersWithInternalApiKey();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        userServiceRestClient.exchange(
                userServiceUrl + "/api/v1/users/" + id,
                HttpMethod.DELETE,
                request,
                Void.class
        );
    }

    private HttpHeaders headersWithInternalApiKey() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(INTERNAL_SERVICE_API_KEY_HEADER, internalApiKey);

        return headers;
    }

}

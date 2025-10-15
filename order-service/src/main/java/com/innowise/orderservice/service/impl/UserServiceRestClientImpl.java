package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.model.dto.UserResponse;
import com.innowise.orderservice.service.UserServiceRestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceRestClientImpl implements UserServiceRestClient {

    @Qualifier("userServiceRestTemplate")
    private final RestTemplate userServiceRestTemplate;

    @Value("${user-service.url}")
    private String userServiceUrl;

    public UserServiceRestClientImpl(@Qualifier("userServiceRestTemplate") RestTemplate userServiceRestTemplate) {
        this.userServiceRestTemplate = userServiceRestTemplate;
    }

    @Override
    public UserResponse findUserById(Long userId) {
        return userServiceRestTemplate.getForObject(
                userServiceUrl + "/users/{userId}",
                UserResponse.class,
                userId
        );
    }

    @Override
    public List<UserResponse> findUsersByIds(Set<Long> userIds) {
        String idsString = userIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        String url = userServiceUrl + "/users?filter=ids&ids={ids}";

        ResponseEntity<List<UserResponse>> response = userServiceRestTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {},
                idsString
        );

        return response.getBody();
    }

}

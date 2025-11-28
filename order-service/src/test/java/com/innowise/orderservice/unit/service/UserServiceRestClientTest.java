package com.innowise.orderservice.unit.service;

import com.innowise.orderservice.model.dto.UserResponse;
import com.innowise.orderservice.service.impl.UserServiceRestClientImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Set;

import static com.innowise.orderservice.util.OrderUtil.createUserResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceRestClientTest {

    @InjectMocks
    private UserServiceRestClientImpl userServiceRestClient;

    @Mock
    private RestTemplate userServiceRestTemplate;

    private final String userServiceUrl = "http://localhost:8081";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userServiceRestClient, "userServiceUrl", userServiceUrl);
    }

    @Test
    @DisplayName("Should return user by id successfully")
    void findUserById_ShouldReturnUser_WhenUserExists() {
        Long userId = 1L;
        UserResponse expectedResponse = createUserResponse(userId);
        String url = userServiceUrl + "/api/v1/users/{userId}";

        when(userServiceRestTemplate.getForObject(url, UserResponse.class, userId))
                .thenReturn(expectedResponse);

        UserResponse actualResponse = userServiceRestClient.findUserById(userId);

        assertThat(actualResponse).isNotNull()
                .isEqualTo(expectedResponse);

        verify(userServiceRestTemplate).getForObject(url, UserResponse.class, userId);
    }

    @Test
    @DisplayName("Should return users by ids successfully")
    void findUsersByIds_ShouldReturnUsers_WhenUsersExist() {
        Set<Long> userIds = Set.of(1L, 2L, 3L);
        List<UserResponse> expectedResponses = List.of(
                createUserResponse(1L),
                createUserResponse(2L),
                createUserResponse(3L)
        );
        String url = userServiceUrl + "/api/v1/users?filter=ids&ids={ids}";

        when(userServiceRestTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class),
                anyString()
        )).thenReturn(ResponseEntity.ok(expectedResponses));

        List<UserResponse> actualResponses = userServiceRestClient.findUsersByIds(userIds);

        assertThat(actualResponses).isNotNull()
                .hasSize(3)
                .containsExactlyInAnyOrderElementsOf(expectedResponses);

        verify(userServiceRestTemplate).exchange(
                eq(url),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class),
                anyString()
        );
    }

}

package com.innowise.orderservice.integration.service;

import com.innowise.orderservice.exception.ResourceNotFoundException;
import com.innowise.orderservice.integration.BaseIntegrationTest;
import com.innowise.orderservice.model.dto.UserResponse;
import com.innowise.orderservice.service.UserServiceRestClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.reset;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.innowise.orderservice.util.SecurityUtil.clearAuthentication;
import static com.innowise.orderservice.util.SecurityUtil.setupAuthentication;
import static com.innowise.orderservice.util.WireMockStubUtil.createUserResponse;
import static com.innowise.orderservice.util.WireMockStubUtil.stubUserServiceFindById;
import static com.innowise.orderservice.util.WireMockStubUtil.stubUserServiceFindByIds;
import static com.innowise.orderservice.util.WireMockStubUtil.stubUserServiceNotFound;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceRestClientIT extends BaseIntegrationTest {

    @Autowired
    private UserServiceRestClient userServiceRestClient;

    @BeforeEach
    void setUp() {
        setupAuthentication(1L);
        reset();
    }

    @AfterEach
    void shutDown() {
        clearAuthentication();
    }

    @Test
    @DisplayName("Should return user by id with real HTTP call")
    void findUserById_ShouldReturnUser_WhenUserExists() {
        Long userId = 1L;
        stubUserServiceFindById(userId, "John Doe", "john@example.com");

        UserResponse response = userServiceRestClient.findUserById(userId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("John Doe");
        assertThat(response.getEmail()).isEqualTo("john@example.com");

        verify(getRequestedFor(urlEqualTo("/api/v1/users/" + userId)));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void findUserById_ShouldThrowException_WhenUser404() {
        Long userId = 999L;

        stubUserServiceNotFound(userId);

        assertThatThrownBy(() -> userServiceRestClient.findUserById(userId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(getRequestedFor(urlEqualTo("/api/v1/users/" + userId)));
    }

    @Test
    @DisplayName("Should return users by ids with real HTTP call")
    void findUsersByIds_ShouldReturnUsers_WhenUsersExist() {
        Set<Long> userIds = Set.of(1L, 2L);

        stubUserServiceFindByIds(List.of(
                createUserResponse(1L, "User 1", "user1@example.com"),
                createUserResponse(2L, "User 2", "user2@example.com")
        ));

        List<UserResponse> responses = userServiceRestClient.findUsersByIds(userIds);

        assertThat(responses).isNotNull()
                .hasSize(2)
                .extracting(UserResponse::getId)
                .containsExactlyInAnyOrder(1L, 2L);

        verify(getRequestedFor(urlPathEqualTo("/api/v1/users"))
                .withQueryParam("filter", equalTo("ids"))
                .withQueryParam("ids", matching(".*")));
    }

}


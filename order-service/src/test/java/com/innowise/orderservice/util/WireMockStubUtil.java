package com.innowise.orderservice.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.orderservice.model.dto.UserResponse;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;


public class WireMockStubUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private WireMockStubUtil() {
    }

    public static void stubUserServiceFindById(Long userId, String name, String email) {
        String json = buildUserJson(userId, name, email);

        stubFor(get(urlEqualTo("/users/" + userId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(json)));
    }

    public static void stubUserServiceNotFound(Long userId) {
        stubFor(get(urlEqualTo("/users/" + userId))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody(buildErrorJson("User not found"))));
    }

    public static void stubUserServiceFindByIds(List<UserResponse> users) {
        try {
            String json = objectMapper.writeValueAsString(users);

            stubFor(get(urlPathEqualTo("/users"))
                    .withQueryParam("filter", equalTo("ids"))
                    .withQueryParam("ids", matching(".*"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(json)));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize users to JSON", e);
        }
    }

    private static String buildUserJson(Long id, String name, String email) {
        return String.format("""
                {
                    "id": %d,
                    "name": "%s",
                    "email": "%s"
                }
                """, id, name, email);
    }

    private static String buildErrorJson(String message) {
        return String.format("""
                {
                    "message": "%s"
                }
                """, message);
    }

    public static UserResponse createUserResponse(Long id, String name, String email) {
        return UserResponse.builder()
                .id(id)
                .name(name)
                .email(email)
                .build();
    }

}


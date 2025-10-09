package com.innowise.authenticationservice.unit.util;

import com.innowise.authenticationservice.model.RoleEnum;
import com.innowise.authenticationservice.model.dto.AuthRequest;
import com.innowise.authenticationservice.model.dto.TokenInfoResponse;
import com.innowise.authenticationservice.model.dto.TokenResponse;
import com.innowise.authenticationservice.model.dto.UserDto;

import java.util.List;

public class TestDataFactory {

    public static final String TEST_EMAIL = "nikita@gmail.com";
    public static final String TEST_PASSWORD = "password123";
    public static final Long TEST_USER_ID = 1L;
    public static final String TEST_ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.access.token";
    public static final String TEST_REFRESH_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh.token";
    public static final String TEST_BEARER_TOKEN = "Bearer " + TEST_ACCESS_TOKEN;

    private TestDataFactory() {}

    public static AuthRequest createAuthRequest() {
        return AuthRequest.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .build();
    }

    public static AuthRequest createAuthRequest(String email, String password) {
        return AuthRequest.builder()
                .email(email)
                .password(password)
                .build();
    }

    public static UserDto createUserDto() {
        return UserDto.builder()
                .id(TEST_USER_ID)
                .email(TEST_EMAIL)
                .roles(List.of(RoleEnum.ROLE_USER))
                .build();
    }

    public static UserDto createUserDto(Long id, String email, List<RoleEnum> roles) {
        return UserDto.builder()
                .id(id)
                .email(email)
                .roles(roles)
                .build();
    }

    public static TokenResponse createTokenResponse() {
        return TokenResponse.builder()
                .accessToken(TEST_ACCESS_TOKEN)
                .refreshToken(TEST_REFRESH_TOKEN)
                .build();
    }

    public static TokenResponse createTokenResponse(String accessToken, String refreshToken) {
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public static TokenInfoResponse createTokenInfoResponse() {
        return TokenInfoResponse.builder()
                .userId(TEST_USER_ID)
                .email(TEST_EMAIL)
                .roles(List.of(RoleEnum.ROLE_USER))
                .build();
    }


    public static TokenInfoResponse createTokenInfoResponse(Long userId, String email, List<RoleEnum> roles) {
        return TokenInfoResponse.builder()
                .userId(userId)
                .email(email)
                .roles(roles)
                .build();
    }

}


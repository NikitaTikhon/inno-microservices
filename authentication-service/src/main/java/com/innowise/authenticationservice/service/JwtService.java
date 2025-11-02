package com.innowise.authenticationservice.service;

import com.innowise.authenticationservice.model.RoleEnum;
import com.innowise.authenticationservice.model.dto.UserDto;

import java.util.List;

/**
 * Service interface for JWT token operations.
 * Defines the contract for generating, validating, and extracting claims from JWT tokens.
 */
public interface JwtService {

    /**
     * Generates an access JWT token for the given user.
     *
     * @param user The DTO containing the user's data.
     * @return The generated access token as a string.
     */
    String generateAccessToken(UserDto user);

    /**
     * Generates a refresh JWT token for the given user.
     *
     * @param user The DTO containing the user's data.
     * @return The generated refresh token as a string.
     */
    String generateRefreshToken(UserDto user);

    /**
     * Extracts the user ID from the JWT token.
     *
     * @param token The JWT token.
     * @return The user ID extracted from the token.
     */
    Long extractUserId(String token);

    /**
     * Extracts the list of roles from the JWT token.
     *
     * @param token The JWT token.
     * @return The list of roles extracted from the token.
     */
    List<RoleEnum> extractRoles(String token);

    /**
     * Extracts the token type from the JWT token.
     *
     * @param token The JWT token.
     * @return The token type (access or refresh) extracted from the token.
     */
    String extractTokenType(String token);

    /**
     * Validates the JWT token by checking its expiration.
     *
     * @param token The JWT token to validate.
     * @return {@code true} if the token is valid, {@code false} otherwise.
     */
    boolean isTokenValid(String token);

    /**
     * Validates the refresh JWT token by checking its expiration and type.
     *
     * @param token The JWT token to validate.
     * @return {@code true} if the refresh token is valid, {@code false} otherwise.
     */
    boolean isRefreshTokenValid(String token);

}

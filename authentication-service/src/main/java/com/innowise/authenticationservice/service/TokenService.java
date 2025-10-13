package com.innowise.authenticationservice.service;

import com.innowise.authenticationservice.model.dto.AuthRequest;
import com.innowise.authenticationservice.model.dto.TokenInfoResponse;
import com.innowise.authenticationservice.model.dto.TokenResponse;

/**
 * Service interface for managing token-related business logic.
 * Defines the contract for creating, refreshing, and validating JWT tokens.
 */
public interface TokenService {

    /**
     * Creates a new pair of JWT tokens (access and refresh) for the authenticated user.
     * Validates the user's credentials before generating tokens.
     *
     * @param authRequest The DTO containing the user's email and password.
     * @return The DTO containing the generated access and refresh tokens.
     * @throws org.springframework.security.authentication.BadCredentialsException if the credentials are invalid.
     */
    TokenResponse createToken(AuthRequest authRequest);

    /**
     * Refreshes the JWT tokens using a valid refresh token.
     * Generates a new pair of access and refresh tokens.
     *
     * @param authHeader The Authorization header containing the refresh token.
     * @return The DTO containing the new access and refresh tokens.
     * @throws com.innowise.authenticationservice.exception.HeaderException if the authorization header is invalid
     * @throws org.springframework.security.authentication.BadCredentialsException if the user is not found.
     * @throws com.innowise.authenticationservice.exception.TokenException if the refresh token is invalid or expired.
     */
    TokenResponse refreshToken(String authHeader);

    /**
     * Validates the JWT token and extracts user information from it.
     *
     * @param authHeader The Authorization header containing the token to validate.
     * @return The DTO containing the user's ID, email, and roles extracted from the token.
     * @throws com.innowise.authenticationservice.exception.HeaderException if the authorization header is invalid.
     * @throws com.innowise.authenticationservice.exception.TokenException if the token is invalid or expired.
     */
    TokenInfoResponse validateToken(String authHeader);

}

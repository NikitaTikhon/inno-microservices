package com.innowise.authenticationservice.controller;

import com.innowise.authenticationservice.model.dto.AuthRequest;
import com.innowise.authenticationservice.model.dto.RegistrationRequest;
import com.innowise.authenticationservice.model.dto.TokenInfoResponse;
import com.innowise.authenticationservice.model.dto.TokenResponse;
import com.innowise.authenticationservice.model.dto.UserDto;
import com.innowise.authenticationservice.service.TokenService;
import com.innowise.authenticationservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.innowise.authenticationservice.config.SecurityConstant.AUTHORIZATION_HEADER;

/**
 * Controller for managing authentication and authorization operations.
 * Provides REST endpoints for user registration, login, token refresh, and token validation.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final UserService userService;
    private final TokenService tokenService;

    /**
     * Registers a new user in the system.
     * The password is hashed using BCrypt before storing.
     *
     * @param registrationRequest The {@link RegistrationRequest} object containing the user's email and password.
     * @return A {@link ResponseEntity} with the created {@link UserDto} object and an HTTP status of OK (200).
     */
    @PostMapping("/registration")
    public ResponseEntity<UserDto> saveUser(@RequestBody @Valid RegistrationRequest registrationRequest) {
        UserDto user = userService.save(registrationRequest);

        return ResponseEntity.ok(user);
    }

    /**
     * Authenticates a user and generates JWT tokens.
     * Returns both access and refresh tokens upon successful authentication.
     *
     * @param authRequest The {@link AuthRequest} object containing the user's email and password.
     * @return A {@link ResponseEntity} with the {@link TokenResponse} object containing access and refresh tokens and an HTTP status of OK (200).
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> createToken(@RequestBody @Valid AuthRequest authRequest) {
        TokenResponse token = tokenService.createToken(authRequest);

        return ResponseEntity.ok(token);
    }

    /**
     * Refreshes JWT tokens using a valid refresh token.
     * Generates a new pair of access and refresh tokens.
     *
     * @param authHeader The Authorization header containing the refresh token (Bearer token).
     * @return A {@link ResponseEntity} with the {@link TokenResponse} object containing new access and refresh tokens and an HTTP status of OK (200).
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@RequestHeader(name = AUTHORIZATION_HEADER) String authHeader) {
        TokenResponse token = tokenService.refreshToken(authHeader);

        return ResponseEntity.ok(token);
    }

    /**
     * Validates a JWT token and extracts user information from it.
     * Returns the user's ID, email, and roles if the token is valid.
     *
     * @param authHeader The Authorization header containing the token to validate (Bearer token).
     * @return A {@link ResponseEntity} with the {@link TokenInfoResponse} object containing user information and an HTTP status of OK (200).
     */
    @PostMapping("/validate")
    public ResponseEntity<TokenInfoResponse> validateToken(@RequestHeader(name = AUTHORIZATION_HEADER) String authHeader) {
        TokenInfoResponse tokenInfo = tokenService.validateToken(authHeader);

        return ResponseEntity.ok(tokenInfo);
    }

}

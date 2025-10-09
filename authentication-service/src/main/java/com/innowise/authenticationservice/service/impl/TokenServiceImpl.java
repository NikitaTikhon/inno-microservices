package com.innowise.authenticationservice.service.impl;

import com.innowise.authenticationservice.exception.HeaderException;
import com.innowise.authenticationservice.exception.TokenException;
import com.innowise.authenticationservice.model.dto.AuthRequest;
import com.innowise.authenticationservice.model.dto.TokenInfoResponse;
import com.innowise.authenticationservice.model.dto.TokenResponse;
import com.innowise.authenticationservice.model.dto.UserDto;
import com.innowise.authenticationservice.model.entity.User;
import com.innowise.authenticationservice.repository.UserRepository;
import com.innowise.authenticationservice.service.JwtService;
import com.innowise.authenticationservice.service.TokenService;
import com.innowise.authenticationservice.util.ExceptionMessageGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.innowise.authenticationservice.config.constant.SecurityConstant.AUTHORIZATION_BEARER_PREFIX;
import static com.innowise.authenticationservice.config.constant.SecurityConstant.AUTHORIZATION_TOKEN_POSITION;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public TokenResponse createToken(AuthRequest authRequest) {
        User user = userRepository.findByEmail(authRequest.getEmail())
                .orElseThrow(() -> new BadCredentialsException(ExceptionMessageGenerator.userBadCredentials()));

        if (!passwordEncoder.matches(authRequest.getPassword(), user.getPassword())) {
            throw new BadCredentialsException(ExceptionMessageGenerator.userBadCredentials());
        }

        UserDto userDto = UserDto.of(user);

        String accessToken = jwtService.generateAccessToken(userDto);
        String refreshToken = jwtService.generateRefreshToken(userDto);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TokenResponse refreshToken(String authHeader) {
        validateAuthorizationHeader(authHeader);

        String token = authHeader.substring(AUTHORIZATION_TOKEN_POSITION);
        String email = jwtService.extractEmail(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException(ExceptionMessageGenerator.userBadCredentials()));

        if (!jwtService.isRefreshTokenValid(token)) {
            throw new TokenException(ExceptionMessageGenerator.tokenInvalid());
        }

        UserDto userDto = UserDto.of(user);

        String accessToken = jwtService.generateAccessToken(userDto);
        String refreshToken = jwtService.generateRefreshToken(userDto);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public TokenInfoResponse validateToken(String authHeader) {
        validateAuthorizationHeader(authHeader);

        String token = authHeader.substring(AUTHORIZATION_TOKEN_POSITION);

        if (!jwtService.isTokenValid(token)) {
            throw new TokenException(ExceptionMessageGenerator.tokenInvalid());
        }

        return TokenInfoResponse.builder()
                .userId(jwtService.extractUserId(token))
                .email(jwtService.extractEmail(token))
                .roles(jwtService.extractRoles(token))
                .build();
    }

    private void validateAuthorizationHeader(String authorizationHeader) {
        if (authorizationHeader == null) {
            throw new HeaderException(ExceptionMessageGenerator.authHeaderMissing());
        } else if (!authorizationHeader.startsWith(AUTHORIZATION_BEARER_PREFIX)) {
            throw new HeaderException(ExceptionMessageGenerator.authHeaderWrong());
        }
    }

}

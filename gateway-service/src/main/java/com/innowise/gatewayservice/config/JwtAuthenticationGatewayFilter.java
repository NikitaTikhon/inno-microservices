package com.innowise.gatewayservice.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.gatewayservice.model.RoleEnum;
import com.innowise.gatewayservice.model.dto.ErrorApiDto;
import com.innowise.gatewayservice.util.ExceptionMessageGenerator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import static com.innowise.gatewayservice.config.constant.SecurityConstant.AUTHORIZATION_BEARER_PREFIX;
import static com.innowise.gatewayservice.config.constant.SecurityConstant.AUTHORIZATION_HEADER;
import static com.innowise.gatewayservice.config.constant.SecurityConstant.AUTHORIZATION_TOKEN_POSITION;
import static com.innowise.gatewayservice.config.constant.SecurityConstant.TOKEN_CLAIM_ROLES;
import static com.innowise.gatewayservice.config.constant.SecurityConstant.TOKEN_CLAIM_USER_ID;

@Component
public class JwtAuthenticationGatewayFilter extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilter.Config> {

    @Value("${security.jwt.secret_key}")
    private String secretKey;

    private final ObjectMapper objectMapper;

    public JwtAuthenticationGatewayFilter(ObjectMapper objectMapper) {
        super(Config.class);
        this.objectMapper = objectMapper;
    }

    /**
     * Applies JWT authentication filter to gateway routes.
     * If validation fails at any step, returns 401 Unauthorized with detailed error message.
     *
     * @param config The filter configuration (currently empty, reserved for future use).
     * @return A {@link GatewayFilter} that performs JWT authentication.
     */
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst(AUTHORIZATION_HEADER);

            if (authHeader == null || !authHeader.startsWith(AUTHORIZATION_BEARER_PREFIX)) {
                return onError(exchange, ExceptionMessageGenerator.missingOrInvalidHeader(), HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(AUTHORIZATION_TOKEN_POSITION);

            try {
                if (!isTokenValid(token)) {
                    return onError(exchange, ExceptionMessageGenerator.tokenExpired(), HttpStatus.UNAUTHORIZED);
                }

                Long userId = extractUserId(token);

                if (userId == null) {
                    return onError(exchange, ExceptionMessageGenerator.tokenInvalidClaims(), HttpStatus.UNAUTHORIZED);
                }

                return chain.filter(exchange);

            } catch (JwtException ex) {
                return onError(exchange, ex.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        };
    }

    /**
     * Handles authentication errors by creating a standardized error response.
     *
     * @param exchange The {@link ServerWebExchange} representing the current request/response.
     * @param message The error message to include in the response.
     * @param httpStatus The {@link HttpStatus} to set for the response.
     * @return A {@link Mono} that completes when the error response is written.
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        ErrorApiDto errorApiDto = ErrorApiDto.builder()
                .timestamp(LocalDateTime.now())
                .status(httpStatus.value())
                .error(httpStatus.getReasonPhrase())
                .message(message)
                .path(exchange.getRequest().getURI().getPath())
                .build();

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorApiDto);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            return response.setComplete();
        }
    }

    /**
     * Configuration class for JWT Authentication Gateway Filter.
     */
    public static class Config {
    }

    /**
     * Retrieves the signing key used for JWT token validation.
     * Decodes the Base64URL encoded secret key and creates an HMAC SHA key.
     *
     * @return The {@link SecretKey} for JWT validation.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64URL.decode(secretKey);

        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extracts a specific claim from the JWT token.
     *
     * @param token    The JWT token.
     * @param resolver A function to extract the desired claim from the Claims object.
     * @param <T>      The type of the claim to extract.
     * @return The extracted claim value.
     */
    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);

        return resolver.apply(claims);
    }

    /**
     * Extracts all claims from the JWT token.
     * Parses and validates the token signature using the signing key.
     *
     * @param token The JWT token to parse.
     * @return The {@link Claims} object containing all token claims.
     */
    private Claims extractAllClaims(String token) {
        JwtParserBuilder parser = Jwts.parser();
        parser.verifyWith(getSigningKey());

        return parser.build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extracts the user ID from the JWT token.
     *
     * @param token The JWT token.
     * @return The user ID extracted from the token.
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get(TOKEN_CLAIM_USER_ID, Long.class));
    }

    /**
     * Extracts the list of roles from the JWT token.
     *
     * @param token The JWT token.
     * @return The list of {@link RoleEnum} extracted from the token.
     */
    public List<RoleEnum> extractRoles(String token) {
        List<?> rawRoles = extractClaim(token, claims -> claims.get(TOKEN_CLAIM_ROLES, List.class));

        if (rawRoles == null || rawRoles.isEmpty()) {
            return List.of();
        }

        return rawRoles.stream()
                .map(Object::toString)
                .map(RoleEnum::valueOf)
                .toList();
    }

    /**
     * Extracts the expiration date from the JWT token.
     *
     * @param token The JWT token.
     * @return The expiration {@link Date} of the token.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Validates the JWT token by checking if it's not expired.
     *
     * @param token The JWT token to validate.
     * @return {@code true} if the token is valid, {@code false} otherwise.
     */
    public boolean isTokenValid(String token) {
        return !isTokenExpired(token);
    }

    /**
     * Checks if the JWT token is expired.
     *
     * @param token The JWT token.
     * @return {@code true} if the token is expired, {@code false} otherwise.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

}

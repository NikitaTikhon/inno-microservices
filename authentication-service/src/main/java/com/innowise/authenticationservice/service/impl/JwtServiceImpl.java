package com.innowise.authenticationservice.service.impl;

import com.innowise.authenticationservice.model.RoleEnum;
import com.innowise.authenticationservice.model.dto.UserDto;
import com.innowise.authenticationservice.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.innowise.authenticationservice.config.constant.SecurityConstant.TOKEN_ACCESS_TYPE;
import static com.innowise.authenticationservice.config.constant.SecurityConstant.TOKEN_CLAIM_ROLES;
import static com.innowise.authenticationservice.config.constant.SecurityConstant.TOKEN_CLAIM_TYPE;
import static com.innowise.authenticationservice.config.constant.SecurityConstant.TOKEN_CLAIM_USER_ID;
import static com.innowise.authenticationservice.config.constant.SecurityConstant.TOKEN_REFRESH_TYPE;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    @Value("${security.jwt.secret_key}")
    private String secretKey;

    @Value("${security.jwt.access_token_expiration}")
    private Long accessTokenExpiration;

    @Value("${security.jwt.refresh_token_expiration}")
    private Long refreshTokenExpiration;

    @Override
    public String generateAccessToken(UserDto userDto) {
        Map<String, Object> claims = createClaims(userDto, TOKEN_ACCESS_TYPE);

        return generateToken(claims, accessTokenExpiration);
    }

    @Override
    public String generateRefreshToken(UserDto userDto) {
        Map<String, Object> claims = createClaims(userDto, TOKEN_REFRESH_TYPE);

        return generateToken(claims, refreshTokenExpiration);
    }

    private Map<String, Object> createClaims(UserDto userDto, String tokenType) {
        Map<String, Object> claims = new HashMap<>();

        claims.put(TOKEN_CLAIM_TYPE, tokenType);
        claims.put(TOKEN_CLAIM_USER_ID, userDto.getId());
        claims.put(TOKEN_CLAIM_ROLES, userDto.getRoles());

        return claims;
    }

    private String generateToken(Map<String, Object> claims, Long expiryTime) {
        JwtBuilder builder = Jwts.builder()
                .issuedAt(new Date())
                .claims(claims)
                .expiration(new Date(System.currentTimeMillis() + expiryTime))
                .signWith(getSigningKey());

        return builder.compact();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64URL.decode(secretKey);

        return Keys.hmacShaKeyFor(keyBytes);
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);

        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        JwtParserBuilder parser = Jwts.parser();
        parser.verifyWith(getSigningKey());

        return parser.build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get(TOKEN_CLAIM_USER_ID, Long.class));
    }

    @Override
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

    @Override
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get(TOKEN_CLAIM_TYPE, String.class));
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    @Override
    public boolean isTokenValid(String token) {
        return !isTokenExpired(token);
    }

    @Override
    public boolean isRefreshTokenValid(String token) {
        return !isTokenExpired(token)
                && TOKEN_REFRESH_TYPE.equals(extractTokenType(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }


}

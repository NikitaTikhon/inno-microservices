package com.innowise.userservice.config;

import com.innowise.userservice.model.AuthUser;
import com.innowise.userservice.model.RoleEnum;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import static com.innowise.userservice.config.constant.SecurityConstant.AUTHORIZATION_BEARER_PREFIX;
import static com.innowise.userservice.config.constant.SecurityConstant.AUTHORIZATION_HEADER;
import static com.innowise.userservice.config.constant.SecurityConstant.AUTHORIZATION_TOKEN_POSITION;
import static com.innowise.userservice.config.constant.SecurityConstant.TOKEN_CLAIM_ROLES;
import static com.innowise.userservice.config.constant.SecurityConstant.TOKEN_CLAIM_USER_ID;

/**
 * JWT authentication filter that intercepts HTTP requests to validate JWT tokens.
 * This filter extracts the JWT token from the Authorization header, validates it,
 * and sets the authentication in the Spring Security context.
 * <p>
 * The filter runs once per request and processes Bearer tokens from the Authorization header.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${security.jwt.secret_key}")
    private String secretKey;

    /**
     * Filters incoming HTTP requests to extract and validate JWT tokens.
     * <p>
     * Process flow:
     * 1. Extracts the Authorization header
     * 2. Validates the Bearer token format
     * 3. Parses and validates the JWT token
     * 4. Extracts user information (userId, email, roles) from token claims
     * 5. Creates an AuthUser object and sets it in the SecurityContext
     * 6. Continues the filter chain
     *
     * @param request The HTTP request to filter.
     * @param response The HTTP response.
     * @param filterChain The filter chain to continue processing.
     * @throws ServletException if an error occurs during filtering.
     * @throws IOException if an I/O error occurs during filtering.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader != null && authHeader.startsWith(AUTHORIZATION_BEARER_PREFIX)) {
            String token = authHeader.substring(AUTHORIZATION_TOKEN_POSITION);

            if (isTokenValid(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                Long userId = extractUserId(token);
                String email = extractEmail(token);
                List<RoleEnum> roles = extractRoles(token);

                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(RoleEnum::name)
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                AuthUser authUser = AuthUser.builder()
                        .id(userId)
                        .email(email)
                        .authorities(authorities)
                        .build();

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(authUser, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
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
     * @param token The JWT token.
     * @param resolver A function to extract the desired claim from the Claims object.
     * @param <T> The type of the claim to extract.
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
     * Extracts the email (subject) from the JWT token.
     *
     * @param token The JWT token.
     * @return The email extracted from the token.
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
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

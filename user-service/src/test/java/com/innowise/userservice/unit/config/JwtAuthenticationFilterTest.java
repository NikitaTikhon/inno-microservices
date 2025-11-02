package com.innowise.userservice.unit.config;

import com.innowise.userservice.config.JwtAuthenticationFilter;
import com.innowise.userservice.model.AuthUser;
import com.innowise.userservice.model.RoleEnum;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static com.innowise.userservice.config.SecurityConstant.AUTHORIZATION_BEARER_PREFIX;
import static com.innowise.userservice.config.SecurityConstant.AUTHORIZATION_HEADER;
import static com.innowise.userservice.config.SecurityConstant.INTERNAL_SERVICE_API_KEY_HEADER;
import static com.innowise.userservice.config.SecurityConstant.TOKEN_CLAIM_ROLES;
import static com.innowise.userservice.config.SecurityConstant.TOKEN_CLAIM_USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private static final String SECRET_KEY = "test-jwt-secret-key-for-unit-tests-must-be-at-least-512-bits-long-for-HS512-algorithm-security";
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_EMAIL = "test@example.com";
    private static final List<RoleEnum> TEST_ROLES = List.of(RoleEnum.ROLE_USER, RoleEnum.ROLE_ADMIN);

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter();
        String encodedKey = Base64.getUrlEncoder().encodeToString(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        ReflectionTestUtils.setField(jwtAuthenticationFilter, "secretKey", encodedKey);
        ReflectionTestUtils.setField(jwtAuthenticationFilter, "internalApiKey", "test-internal-api-key");
        SecurityContextHolder.clearContext();

        lenient().when(request.getHeader(INTERNAL_SERVICE_API_KEY_HEADER)).thenReturn(null);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should set authentication when valid JWT token is provided")
    void doFilterInternal_WithValidToken_ShouldSetAuthentication() throws ServletException, IOException {
        String validToken = generateValidToken(TEST_USER_ID, TEST_EMAIL, TEST_ROLES, 3600000);
        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(AUTHORIZATION_BEARER_PREFIX + validToken);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isInstanceOf(AuthUser.class);

        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        assertThat(authUser.getId()).isEqualTo(TEST_USER_ID);
        assertThat(authUser.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(authUser.getAuthorities()).hasSize(2);
        assertThat(authUser.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder(RoleEnum.ROLE_USER.name(), RoleEnum.ROLE_ADMIN.name());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should set authentication when valid internal API key is provided")
    void doFilterInternal_WithValidInternalApiKey_ShouldSetAuthentication() throws ServletException, IOException {
        when(request.getHeader(INTERNAL_SERVICE_API_KEY_HEADER)).thenReturn("test-internal-api-key");
        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(null);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo("internal-service");
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("INTERNAL_SERVICE");

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not set authentication when Authorization header is missing")
    void doFilterInternal_WithoutAuthorizationHeader_ShouldNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(null);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not set authentication when token is missing Bearer prefix")
    void doFilterInternal_WithTokenWithoutBearerPrefix_ShouldNotSetAuthentication() throws ServletException, IOException {
        String validToken = generateValidToken(TEST_USER_ID, TEST_EMAIL, TEST_ROLES, 3600000);
        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(validToken);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should throw JwtException when JWT token is expired")
    void doFilterInternal_WithExpiredToken_ShouldThrowJwtException() {
        String expiredToken = generateValidToken(TEST_USER_ID, TEST_EMAIL, TEST_ROLES, -3600000);
        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(AUTHORIZATION_BEARER_PREFIX + expiredToken);

        assertThatThrownBy(() -> jwtAuthenticationFilter.doFilter(request, response, filterChain))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("Should throw JwtException when JWT token has invalid signature")
    void doFilterInternal_WithInvalidTokenSignature_ShouldThrowJwtException() {
        String tokenWithInvalidSignature = generateTokenWithDifferentKey(TEST_USER_ID, TEST_EMAIL);
        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(AUTHORIZATION_BEARER_PREFIX + tokenWithInvalidSignature);

        assertThatThrownBy(() -> jwtAuthenticationFilter.doFilter(request, response, filterChain))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("Should extract correct user ID from valid JWT token")
    void extractUserId_WithValidToken_ShouldReturnCorrectUserId() {
        String validToken = generateValidToken(TEST_USER_ID, TEST_EMAIL, TEST_ROLES, 3600000);

        Long userId = jwtAuthenticationFilter.extractUserId(validToken);

        assertThat(userId).isEqualTo(TEST_USER_ID);
    }

    @Test
    @DisplayName("Should extract correct email from valid JWT token")
    void extractEmail_WithValidToken_ShouldReturnCorrectEmail() {
        String validToken = generateValidToken(TEST_USER_ID, TEST_EMAIL, TEST_ROLES, 3600000);

        String email = jwtAuthenticationFilter.extractEmail(validToken);

        assertThat(email).isEqualTo(TEST_EMAIL);
    }

    @Test
    @DisplayName("Should extract correct roles from valid JWT token")
    void extractRoles_WithValidToken_ShouldReturnCorrectRoles() {
        String validToken = generateValidToken(TEST_USER_ID, TEST_EMAIL, TEST_ROLES, 3600000);

        List<RoleEnum> roles = jwtAuthenticationFilter.extractRoles(validToken);

        assertThat(roles).hasSize(2)
                .containsExactlyInAnyOrder(RoleEnum.ROLE_USER, RoleEnum.ROLE_ADMIN);
    }

    @Test
    @DisplayName("Should return empty list when JWT token has no roles")
    void extractRoles_WithEmptyRoles_ShouldReturnEmptyList() {
        String validToken = generateValidToken(TEST_USER_ID, TEST_EMAIL, List.of(), 3600000);

        List<RoleEnum> roles = jwtAuthenticationFilter.extractRoles(validToken);

        assertThat(roles).isEmpty();
    }

    @Test
    @DisplayName("Should return true when JWT token is valid and not expired")
    void isTokenValid_WithNonExpiredToken_ShouldReturnTrue() {
        String validToken = generateValidToken(TEST_USER_ID, TEST_EMAIL, TEST_ROLES, 3600000);

        boolean isValid = jwtAuthenticationFilter.isTokenValid(validToken);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should throw JwtException when validating expired JWT token")
    void isTokenValid_WithExpiredToken_ShouldThrowJwtException() {
        String expiredToken = generateValidToken(TEST_USER_ID, TEST_EMAIL, TEST_ROLES, -3600000);

        assertThatThrownBy(() -> jwtAuthenticationFilter.isTokenValid(expiredToken))
                .isInstanceOf(JwtException.class);
    }

    private String generateValidToken(Long userId, String email, List<RoleEnum> roles, long expirationOffset) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationOffset);

        return Jwts.builder()
                .subject(email)
                .claim(TOKEN_CLAIM_USER_ID, userId)
                .claim(TOKEN_CLAIM_ROLES, roles.stream().map(RoleEnum::name).toList())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    private String generateTokenWithDifferentKey(Long userId, String email) {
        String differentKey = "different-secret-key-for-testing-invalid-signature-must-be-512-bits-long-for-HS512-security";
        SecretKey key = Keys.hmacShaKeyFor(differentKey.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 3600000);

        return Jwts.builder()
                .subject(email)
                .claim(TOKEN_CLAIM_USER_ID, userId)
                .claim(TOKEN_CLAIM_ROLES, List.of(RoleEnum.ROLE_USER.name()))
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

}
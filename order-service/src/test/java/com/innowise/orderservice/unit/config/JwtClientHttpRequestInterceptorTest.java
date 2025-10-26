package com.innowise.orderservice.unit.config;

import com.innowise.orderservice.config.JwtClientHttpRequestInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static com.innowise.orderservice.config.constant.SecurityConstant.AUTHORIZATION_BEARER_PREFIX;
import static com.innowise.orderservice.config.constant.SecurityConstant.AUTHORIZATION_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtClientHttpRequestInterceptorTest {

    private JwtClientHttpRequestInterceptor interceptor;

    @Mock
    private HttpRequest request;

    @Mock
    private ClientHttpRequestExecution execution;

    @Mock
    private ClientHttpResponse response;

    @Mock
    private SecurityContext securityContext;

    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        interceptor = new JwtClientHttpRequestInterceptor();
        headers = new HttpHeaders();
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Should add JWT token to Authorization header when authentication exists with valid token")
    void shouldAddJwtTokenToHeader_WhenAuthenticationExistsWithValidToken() throws IOException {
        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
        Authentication authentication = new UsernamePasswordAuthenticationToken("user", jwtToken);
        
        when(request.getHeaders()).thenReturn(headers);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(execution.execute(any(HttpRequest.class), any(byte[].class))).thenReturn(response);

        ClientHttpResponse result = interceptor.intercept(request, new byte[0], execution);

        assertThat(result).isEqualTo(response);
        assertThat(headers.getFirst(AUTHORIZATION_HEADER))
                .isEqualTo(AUTHORIZATION_BEARER_PREFIX + jwtToken);
    }

    @Test
    @DisplayName("Should not add Authorization header when authentication is null")
    void shouldNotAddAuthorizationHeader_WhenAuthenticationIsNull() throws IOException {
        when(securityContext.getAuthentication()).thenReturn(null);
        when(execution.execute(any(HttpRequest.class), any(byte[].class))).thenReturn(response);

        interceptor.intercept(request, new byte[0], execution);

        assertThat(headers.containsKey(AUTHORIZATION_HEADER)).isFalse();
    }

    @Test
    @DisplayName("Should not add Authorization header when principal is null")
    void shouldNotAddAuthorizationHeader_WhenPrincipalIsNull() throws IOException {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(null);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(execution.execute(any(HttpRequest.class), any(byte[].class))).thenReturn(response);

        interceptor.intercept(request, new byte[0], execution);

        assertThat(headers.containsKey(AUTHORIZATION_HEADER)).isFalse();
    }

    @Test
    @DisplayName("Should not add Authorization header when credentials is not a String")
    void shouldNotAddAuthorizationHeader_WhenCredentialsIsNotString() throws IOException {
        Authentication authentication = new UsernamePasswordAuthenticationToken("user", 12345);
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(execution.execute(any(HttpRequest.class), any(byte[].class))).thenReturn(response);

        interceptor.intercept(request, new byte[0], execution);

        assertThat(headers.containsKey(AUTHORIZATION_HEADER)).isFalse();
    }

    @Test
    @DisplayName("Should not add Authorization header when token is empty or blank")
    void shouldNotAddAuthorizationHeader_WhenTokenIsEmptyOrBlank() throws IOException {
        Authentication authWithEmpty = new UsernamePasswordAuthenticationToken("user", "");
        Authentication authWithBlank = new UsernamePasswordAuthenticationToken("user", "   ");
        
        when(securityContext.getAuthentication()).thenReturn(authWithEmpty);
        when(execution.execute(any(HttpRequest.class), any(byte[].class))).thenReturn(response);
        interceptor.intercept(request, new byte[0], execution);
        assertThat(headers.containsKey(AUTHORIZATION_HEADER)).isFalse();
        
        when(securityContext.getAuthentication()).thenReturn(authWithBlank);
        interceptor.intercept(request, new byte[0], execution);
        assertThat(headers.containsKey(AUTHORIZATION_HEADER)).isFalse();
    }

}

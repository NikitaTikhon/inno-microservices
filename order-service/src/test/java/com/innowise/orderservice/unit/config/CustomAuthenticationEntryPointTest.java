package com.innowise.orderservice.unit.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.orderservice.config.CustomAuthenticationEntryPoint;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;
import java.io.PrintWriter;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationEntryPointTest {

    @InjectMocks
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private PrintWriter writer;

    @Mock
    private AuthenticationException authException;

    @Test
    @DisplayName("Should return 401 Unauthorized status")
    void commence_ShouldReturn401Status() throws IOException {
        when(request.getRequestURI()).thenReturn("/api/orders/1");
        when(response.getWriter()).thenReturn(writer);

        customAuthenticationEntryPoint.commence(request, response, authException);

        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @DisplayName("Should call ObjectMapper to write error response")
    void commence_ShouldCallObjectMapper() throws IOException {
        when(request.getRequestURI()).thenReturn("/api/test");
        when(response.getWriter()).thenReturn(writer);

        customAuthenticationEntryPoint.commence(request, response, authException);

        verify(objectMapper).writeValue(any(PrintWriter.class), any());
    }
    
}

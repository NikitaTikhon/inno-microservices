package com.innowise.authenticationservice.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.authenticationservice.config.RestClientResponseErrorHandler;
import com.innowise.authenticationservice.config.ServiceConfig;
import com.innowise.authenticationservice.controller.AuthenticationController;
import com.innowise.authenticationservice.exception.HeaderException;
import com.innowise.authenticationservice.exception.ResourceAlreadyExistsException;
import com.innowise.authenticationservice.model.RoleEnum;
import com.innowise.authenticationservice.model.dto.AuthRequest;
import com.innowise.authenticationservice.model.dto.RegistrationRequest;
import com.innowise.authenticationservice.model.dto.TokenInfoResponse;
import com.innowise.authenticationservice.model.dto.TokenResponse;
import com.innowise.authenticationservice.model.dto.UserDto;
import com.innowise.authenticationservice.service.TokenService;
import com.innowise.authenticationservice.service.UserService;
import com.innowise.authenticationservice.util.ExceptionMessageGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.innowise.authenticationservice.config.SecurityConstant.AUTHORIZATION_HEADER;
import static com.innowise.authenticationservice.unit.util.TestDataFactory.TEST_ACCESS_TOKEN;
import static com.innowise.authenticationservice.unit.util.TestDataFactory.TEST_BEARER_TOKEN;
import static com.innowise.authenticationservice.unit.util.TestDataFactory.TEST_EMAIL;
import static com.innowise.authenticationservice.unit.util.TestDataFactory.TEST_PASSWORD;
import static com.innowise.authenticationservice.unit.util.TestDataFactory.TEST_REFRESH_TOKEN;
import static com.innowise.authenticationservice.unit.util.TestDataFactory.TEST_USER_ID;
import static com.innowise.authenticationservice.unit.util.TestDataFactory.createAuthRequest;
import static com.innowise.authenticationservice.unit.util.TestDataFactory.createRegistrationRequest;
import static com.innowise.authenticationservice.unit.util.TestDataFactory.createTokenInfoResponse;
import static com.innowise.authenticationservice.unit.util.TestDataFactory.createTokenResponse;
import static com.innowise.authenticationservice.unit.util.TestDataFactory.createUserDto;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(ServiceConfig.class)
@WebMvcTest(AuthenticationController.class)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private TokenService tokenService;
    @MockitoBean
    private RestClientResponseErrorHandler restClientResponseErrorHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should register new user successfully")
    void saveUser_ShouldCreateUser_WhenValidRequest() throws Exception {
        RegistrationRequest registrationRequest = createRegistrationRequest();
        UserDto userDto = createUserDto();

        when(userService.save(any(RegistrationRequest.class))).thenReturn(userDto);

        mockMvc.perform(post("/api/v1/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(TEST_USER_ID))
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.roles[0]").value(RoleEnum.ROLE_USER.name()));
    }

    @Test
    @DisplayName("Should return 400 when email is invalid")
    void saveUser_ShouldReturnBadRequest_WhenEmailIsInvalid() throws Exception {
        AuthRequest authRequest = createAuthRequest("invalid-email", TEST_PASSWORD);

        mockMvc.perform(post("/api/v1/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when password is too short")
    void saveUser_ShouldReturnBadRequest_WhenPasswordIsTooShort() throws Exception {
        AuthRequest authRequest = createAuthRequest(TEST_EMAIL, "12345");

        mockMvc.perform(post("/api/v1/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when email is blank")
    void saveUser_ShouldReturnBadRequest_WhenEmailIsBlank() throws Exception {
        AuthRequest authRequest = createAuthRequest("", TEST_PASSWORD);

        mockMvc.perform(post("/api/v1/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 409 when user already exists")
    void saveUser_ShouldReturnConflict_WhenUserAlreadyExists() throws Exception {
        RegistrationRequest registrationRequest = createRegistrationRequest();

        when(userService.save(any(RegistrationRequest.class)))
                .thenThrow(new ResourceAlreadyExistsException(ExceptionMessageGenerator.userExists(TEST_EMAIL)));

        mockMvc.perform(post("/api/v1/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(ExceptionMessageGenerator.userExists(TEST_EMAIL)));
    }

    @Test
    @DisplayName("Should login user successfully and return tokens")
    void createToken_ShouldReturnTokens_WhenValidCredentials() throws Exception {
        AuthRequest authRequest = createAuthRequest();
        TokenResponse tokenResponse = createTokenResponse();

        when(tokenService.createToken(any(AuthRequest.class))).thenReturn(tokenResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value(TEST_ACCESS_TOKEN))
                .andExpect(jsonPath("$.refreshToken").value(TEST_REFRESH_TOKEN));
    }

    @Test
    @DisplayName("Should return 401 when credentials are invalid")
    void createToken_ShouldReturnUnauthorized_WhenInvalidCredentials() throws Exception {
        AuthRequest authRequest = createAuthRequest();

        when(tokenService.createToken(any(AuthRequest.class)))
                .thenThrow(new BadCredentialsException(ExceptionMessageGenerator.userBadCredentials()));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(ExceptionMessageGenerator.userBadCredentials()));
    }

    @Test
    @DisplayName("Should return 400 when login request is invalid")
    void createToken_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
        AuthRequest authRequest = createAuthRequest("", "");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should refresh tokens successfully")
    void refreshToken_ShouldReturnNewTokens_WhenValidRefreshToken() throws Exception {
        TokenResponse tokenResponse = createTokenResponse();

        when(tokenService.refreshToken(anyString())).thenReturn(tokenResponse);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .header(AUTHORIZATION_HEADER, TEST_BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value(TEST_ACCESS_TOKEN))
                .andExpect(jsonPath("$.refreshToken").value(TEST_REFRESH_TOKEN));
    }

    @Test
    @DisplayName("Should return 400 when refresh token is missing")
    void refreshToken_ShouldReturnBadRequest_WhenTokenIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when refresh token is invalid")
    void refreshToken_ShouldReturnBadRequest_WhenTokenIsInvalid() throws Exception {
        when(tokenService.refreshToken(anyString()))
                .thenThrow(new HeaderException(ExceptionMessageGenerator.authHeaderWrong()));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .header(AUTHORIZATION_HEADER, "Invalid Token"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should validate token successfully and return user info")
    void validateToken_ShouldReturnUserInfo_WhenValidToken() throws Exception {
        TokenInfoResponse tokenInfoResponse = createTokenInfoResponse();

        when(tokenService.validateToken(anyString())).thenReturn(tokenInfoResponse);

        mockMvc.perform(post("/api/v1/auth/validate")
                        .header(AUTHORIZATION_HEADER, TEST_BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(TEST_USER_ID))
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.roles[0]").value(RoleEnum.ROLE_USER.name()));
    }

    @Test
    @DisplayName("Should return 400 when validation token is missing")
    void validateToken_ShouldReturnBadRequest_WhenTokenIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/auth/validate"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when validation token is invalid")
    void validateToken_ShouldReturnBadRequest_WhenTokenIsInvalid() throws Exception {
        when(tokenService.validateToken(anyString()))
                .thenThrow(new HeaderException(ExceptionMessageGenerator.authHeaderWrong()));

        mockMvc.perform(post("/api/v1/auth/validate")
                        .header(AUTHORIZATION_HEADER, "Invalid Token"))
                .andExpect(status().isBadRequest());
    }

}

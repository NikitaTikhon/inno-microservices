package com.innowise.userservice.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.userservice.controller.UserController;
import com.innowise.userservice.exception.MissingRequestParameterException;
import com.innowise.userservice.exception.UserAlreadyExistException;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.model.dto.PageableFilter;
import com.innowise.userservice.model.dto.UserRequest;
import com.innowise.userservice.model.dto.UserResponse;
import com.innowise.userservice.service.UserService;
import com.innowise.userservice.unit.util.UserUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should create user successfully")
    void save_ShouldCreateUser_WhenValidRequest() throws Exception {
        UserRequest userRequest = UserUtil.userRequest();
        UserResponse userResponse = UserUtil.userResponse(1L);

        when(userService.save(any(UserRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Michael"))
                .andExpect(jsonPath("$.surname").value("Walter"))
                .andExpect(jsonPath("$.email").value("michael.walter@gmail.com"));
    }

    @Test
    @DisplayName("Should return 400 when creating user with invalid data")
    void save_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        UserRequest userRequest = UserRequest.builder()
                .name("")
                .surname("Walter")
                .email("invalid-email")
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 409 when creating user with existing email")
    void save_ShouldReturnConflict_WhenEmailAlreadyExists() throws Exception {
        UserRequest userRequest = UserUtil.userRequest();

        when(userService.save(any(UserRequest.class)))
                .thenThrow(new UserAlreadyExistException("User with email already exists"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should return user by id successfully")
    void findById_ShouldReturnUser_WhenUserExists() throws Exception {
        Long userId = 1L;
        UserResponse userResponse = UserUtil.userResponse(userId);

        when(userService.findById(userId)).thenReturn(userResponse);

        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("Michael"))
                .andExpect(jsonPath("$.surname").value("Walter"));
    }

    @Test
    @DisplayName("Should return 404 when user not found by id")
    void findById_ShouldReturnNotFound_WhenUserNotFound() throws Exception {
        Long userId = 1L;

        when(userService.findById(userId))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return users by ids successfully")
    void findByFilter_ShouldReturnUsers_WhenFilterIsIds() throws Exception {
        List<Long> userIds = Arrays.asList(1L, 2L, 3L);
        List<UserResponse> usersResponse = UserUtil.usersResponse(3L);

        when(userService.findByIds(userIds)).thenReturn(usersResponse);

        mockMvc.perform(get("/api/v1/users")
                        .param("filter", "ids")
                        .param("ids", "1", "2", "3"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[2].id").value(3L));
    }

    @Test
    @DisplayName("Should return 400 when ids parameter is missing")
    void findByFilter_ShouldReturnBadRequest_WhenIdsParameterMissing() throws Exception {
        when(userService.findByIds(anyList()))
                .thenThrow(new MissingRequestParameterException("Missing request parameter: ids"));

        mockMvc.perform(get("/api/v1/users")
                        .param("filter", "ids"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return user by email successfully")
    void findByFilter_ShouldReturnUser_WhenFilterIsEmail() throws Exception {
        String email = "michael.walter@gmail.com";
        UserResponse userResponse = UserUtil.userResponse(1L);

        when(userService.findByEmail(email)).thenReturn(userResponse);

        mockMvc.perform(get("/api/v1/users")
                        .param("filter", "email")
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value(email));
    }

    @Test
    @DisplayName("Should return 400 when email parameter is missing")
    void findByFilter_ShouldReturnBadRequest_WhenEmailParameterMissing() throws Exception {
        when(userService.findByEmail(anyString()))
                .thenThrow(new MissingRequestParameterException("Missing request parameter: email"));

        mockMvc.perform(get("/api/v1/users")
                        .param("filter", "email"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when user not found by email")
    void findByFilter_ShouldReturnNotFound_WhenUserNotFoundByEmail() throws Exception {
        String email = "nonexistent@email.com";

        when(userService.findByEmail(email))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/users")
                        .param("filter", "email")
                        .param("email", email))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return all users with pagination")
    void findByFilter_ShouldReturnUsers_WhenFilterIsPageable() throws Exception {
        List<UserResponse> usersResponse = UserUtil.usersResponse(3L);

        when(userService.findAll(any(PageableFilter.class))).thenReturn(usersResponse);

        mockMvc.perform(get("/api/v1/users")
                        .param("filter", "pageable")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @DisplayName("Should return all users with default pageable filter")
    void findByFilter_ShouldReturnUsers_WhenNoFilterSpecified() throws Exception {
        List<UserResponse> usersResponse = UserUtil.usersResponse(3L);

        when(userService.findAll(any(PageableFilter.class))).thenReturn(usersResponse);

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @DisplayName("Should update user successfully")
    void updateById_ShouldUpdateUser_WhenValidRequest() throws Exception {
        Long userId = 1L;
        UserRequest userRequest = UserUtil.userRequest();
        UserResponse userResponse = UserUtil.userResponse(userId);

        when(userService.updateById(userId, userRequest)).thenReturn(userResponse);

        mockMvc.perform(patch("/api/v1/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 400 when updating user with invalid data")
    void updateById_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        Long userId = 1L;
        UserRequest userRequest = UserRequest.builder()
                .name("")
                .surname("Walter")
                .email("invalid-email")
                .build();

        mockMvc.perform(patch("/api/v1/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("Should delete user successfully")
    void deleteById_ShouldDeleteUser_WhenUserExists() throws Exception {
        Long userId = 1L;

        doNothing().when(userService).deleteById(userId);

        mockMvc.perform(delete("/api/v1/users/{id}", userId))
                .andExpect(status().isNoContent());
    }


}

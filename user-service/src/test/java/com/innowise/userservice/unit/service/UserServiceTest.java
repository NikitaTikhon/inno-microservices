package com.innowise.userservice.unit.service;

import com.innowise.userservice.exception.UserAlreadyExistException;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.model.dto.PageableFilter;
import com.innowise.userservice.model.dto.UserRequest;
import com.innowise.userservice.model.dto.UserResponse;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.impl.UserServiceImpl;
import com.innowise.userservice.unit.util.UserUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Test
    @DisplayName("Should create user successfully")
    void save_ShouldCreateUser_WhenEmailDoesNotExist() {
        Long userId = 1L;
        UserRequest userRequest = UserUtil.userRequest();
        User user = UserUtil.user();
        User createdUser = UserUtil.user(userId);
        UserResponse userResponse = UserUtil.userResponse(userId);

        when(userMapper.userRequestToUser(userRequest)).thenReturn(user);
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(false);
        when(userRepository.save(user)).thenReturn(createdUser);
        when(userMapper.userToUserResponse(createdUser)).thenReturn(userResponse);

        UserResponse actualUserResponse = userService.save(userRequest);

        assertThat(actualUserResponse).isNotNull();
        assertThat(actualUserResponse.getId()).isEqualTo(userResponse.getId());
        assertThat(actualUserResponse.getEmail()).isEqualTo(userResponse.getEmail());

        verify(userRepository).existsByEmail(userRequest.getEmail());
        verify(userMapper).userRequestToUser(userRequest);
        verify(userRepository).save(user);
        verify(userMapper).userToUserResponse(createdUser);
    }

    @Test
    @DisplayName("Should return user by id successfully")
    void findById_ShouldReturnUser_WhenUserExists() {
        Long userId = 1L;
        User user = UserUtil.user(userId);
        UserResponse userResponse = UserUtil.userResponse(userId);

        when(userRepository.findByIdWithCards(userId)).thenReturn(Optional.of(user));
        when(userMapper.userToUserResponse(user)).thenReturn(userResponse);

        UserResponse actualUserResponse = userService.findById(userId);

        assertThat(actualUserResponse).isNotNull();
        assertThat(actualUserResponse.getId()).isEqualTo(userResponse.getId());

        verify(userRepository).findByIdWithCards(userId);
        verify(userMapper).userToUserResponse(user);
    }

    @Test
    @DisplayName("Should return users by ids")
    void findByIds_ShouldReturnUsers_WhenUserExists() {
        List<Long> userIds = Arrays.asList(1L, 2L, 3L);
        List<User> users = UserUtil.users(3L);
        List<UserResponse> usersResponse = UserUtil.usersResponse(3L);

        when(userRepository.findByIdIn(userIds)).thenReturn(users);
        when(userMapper.usersToUsersResponseWithoutCards(users)).thenReturn(usersResponse);

        List<UserResponse> actualUsersResponse = userService.findByIds(userIds);

        assertThat(actualUsersResponse).isNotNull()
                .hasSameSizeAs(usersResponse);

        verify(userRepository).findByIdIn(userIds);
        verify(userMapper).usersToUsersResponseWithoutCards(users);
    }

    @Test
    @DisplayName("Should return user by email successfully")
    void findByEmail_ShouldReturnUser_WhenUserExists() {
        String email = "michael.walter@gmail.com";
        User user = UserUtil.user(email);
        UserResponse userResponse = UserUtil.userResponse(1L);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userMapper.userToUserResponseWithoutCards(user)).thenReturn(userResponse);

        UserResponse actualUserResponse = userService.findByEmail(email);

        assertThat(actualUserResponse).isNotNull();
        assertThat(actualUserResponse.getEmail()).isEqualTo(userResponse.getEmail());

        verify(userRepository).findByEmail(email);
        verify(userMapper).userToUserResponseWithoutCards(user);
    }

    @Test
    @DisplayName("Should update user successfully")
    void updateById_ShouldUpdateUser_WhenUserExists() {
        Long userId = 1L;
        UserRequest userRequest = UserUtil.userRequest();
        User existUser = UserUtil.user(userId);
        User updatedUser = UserUtil.user(userId);
        UserResponse userResponse = UserUtil.userResponse(userId);

        when(userRepository.findByIdWithCards(userId)).thenReturn(Optional.of(existUser));
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(false);
        when(userRepository.save(existUser)).thenReturn(updatedUser);
        when(userMapper.userToUserResponse(updatedUser)).thenReturn(userResponse);

        UserResponse actualUserResponse = userService.updateById(userId, userRequest);

        assertThat(actualUserResponse).isNotNull();
        assertThat(actualUserResponse.getId()).isEqualTo(userResponse.getId());
        assertThat(actualUserResponse.getEmail()).isEqualTo(userResponse.getEmail());

        verify(userRepository).findByIdWithCards(userId);
        verify(userRepository).existsByEmail(userRequest.getEmail());
        verify(userMapper).updateUserFromUserRequest(userRequest, existUser);
        verify(userRepository).save(existUser);
        verify(userMapper).userToUserResponse(updatedUser);
    }

    @Test
    @DisplayName("Should delete user successfully")
    void deleteById_ShouldDeleteUser_WhenUserExists() {
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(true);

        userService.deleteById(userId);

        verify(userRepository).existsById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    @DisplayName("Should return all users with pagination")
    void findAll_ShouldReturnUsers_WhenCalled() {
        PageableFilter pageableFilter = PageableFilter.builder()
                .page(0)
                .size(10)
                .build();
        List<User> users = UserUtil.users(3L);
        List<UserResponse> usersResponse = UserUtil.usersResponse(3L);
        Page<User> userPage = new PageImpl<>(users);

        when(userRepository.findAll(PageRequest.of(pageableFilter.getPage(), pageableFilter.getSize())))
                .thenReturn(userPage);
        when(userMapper.usersToUsersResponseWithoutCards(users)).thenReturn(usersResponse);

        List<UserResponse> actualUsersResponse = userService.findAll(pageableFilter);

        assertThat(actualUsersResponse).isNotNull()
                .hasSameSizeAs(usersResponse);

        verify(userRepository).findAll(PageRequest.of(pageableFilter.getPage(), pageableFilter.getSize()));
        verify(userMapper).usersToUsersResponseWithoutCards(users);
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistException when email already exists")
    void save_ShouldThrowException_WhenEmailAlreadyExists() {
        UserRequest userRequest = UserUtil.userRequest();

        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.save(userRequest))
                .isInstanceOf(UserAlreadyExistException.class);

        verify(userRepository).existsByEmail(userRequest.getEmail());
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user not found by id")
    void findById_ShouldThrowException_WhenUserNotFound() {
        Long userId = 1L;

        when(userRepository.findByIdWithCards(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(userId))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findByIdWithCards(userId);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user not found by email")
    void findByEmail_ShouldThrowException_WhenUserNotFound() {
        String email = "nonexistent@email.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByEmail(email))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when updating non-existent user")
    void updateById_ShouldThrowException_WhenUserNotFound() {
        Long userId = 1L;
        UserRequest userRequest = UserUtil.userRequest();

        when(userRepository.findByIdWithCards(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateById(userId, userRequest))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findByIdWithCards(userId);
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistException when updating with existing email")
    void updateById_ShouldThrowException_WhenEmailAlreadyExists() {
        Long userId = 1L;
        UserRequest userRequest = UserUtil.userRequest();
        User existUser = UserUtil.user(userId);
        existUser.setEmail("different@email.com");

        when(userRepository.findByIdWithCards(userId)).thenReturn(Optional.of(existUser));
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.updateById(userId, userRequest))
                .isInstanceOf(UserAlreadyExistException.class);

        verify(userRepository).findByIdWithCards(userId);
        verify(userRepository).existsByEmail(userRequest.getEmail());
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when deleting non-existent user")
    void deleteById_ShouldThrowException_WhenUserNotFound() {
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteById(userId))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).existsById(userId);
    }

}

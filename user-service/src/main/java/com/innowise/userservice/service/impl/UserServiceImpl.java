package com.innowise.userservice.service.impl;

import com.innowise.userservice.exception.UserAlreadyExistException;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.model.dto.UserRequest;
import com.innowise.userservice.model.dto.UserResponse;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for managing user-related business logic.
 * Handles CRUD operations and interaction with the UserRepository.
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse save(UserRequest userRequest) {
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new UserAlreadyExistException("User with email: %s already exists".formatted(userRequest.getEmail()));
        }
        User user = userMapper.userRequestToUser(userRequest);

        return userMapper.userToUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "users", key = "#id")
    public UserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id: %s not found".formatted(id)));

        return userMapper.userToUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findByIds(List<Long> ids) {
        List<User> users = userRepository.findByIdIn(ids);

        return userMapper.usersToUsersResponse(users);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email: %s not found".formatted(email)));

        return userMapper.userToUserResponse(user);
    }

    @Override
    @Transactional
    @CachePut(cacheNames = "users", key = "#id")
    public UserResponse updateById(Long id, UserRequest userRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id: %s not found".formatted(id)));
        if (userRepository.existsByEmail(userRequest.getEmail()) && !user.getEmail().equals(userRequest.getEmail())) {
            throw new UserAlreadyExistException("User with email: %s already exists".formatted(userRequest.getEmail()));
        }

        userMapper.updateUserFromUserRequest(userRequest, user);

        return userMapper.userToUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "users", key = "#id")
    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User with id: %s not found".formatted(id));
        }

        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findAll(Pageable pageable) {
        List<User> users = userRepository.findAll(pageable).getContent();

        return userMapper.usersToUsersResponse(users);
    }

}

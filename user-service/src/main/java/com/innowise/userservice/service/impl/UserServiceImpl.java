package com.innowise.userservice.service.impl;

import com.innowise.userservice.exception.UserAlreadyExistException;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.model.dto.PageableFilter;
import com.innowise.userservice.model.dto.UserRequest;
import com.innowise.userservice.model.dto.UserResponse;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.UserService;
import com.innowise.userservice.util.ExceptionMessageGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
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
            throw new UserAlreadyExistException(ExceptionMessageGenerator.userExist(userRequest.getEmail()));
        }
        User user = userMapper.userRequestToUser(userRequest);

        return userMapper.userToUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "users", key = "#id")
    public UserResponse findById(Long id) {
        User user = userRepository.findByIdWithCards(id)
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessageGenerator.userNotFound(id)));

        return userMapper.userToUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findByIds(List<Long> ids) {
        List<User> users = userRepository.findByIdIn(ids);

        return userMapper.usersToUsersResponseWithoutCards(users);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "usersByEmail", key = "#email")
    public UserResponse findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessageGenerator.userNotFound(email)));

        return userMapper.userToUserResponseWithoutCards(user);
    }

    @Override
    @Transactional
    @CachePut(cacheNames = "users", key = "#id")
    @CacheEvict(cacheNames = "usersByEmail", key = "#result.email")
    public UserResponse updateById(Long id, UserRequest userRequest) {
        User user = userRepository.findByIdWithCards(id)
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessageGenerator.userNotFound(id)));

        if (userRepository.existsByEmail(userRequest.getEmail()) && !user.getEmail().equals(userRequest.getEmail())) {
            throw new UserAlreadyExistException(ExceptionMessageGenerator.userExist(userRequest.getEmail()));
        }

        userMapper.updateUserFromUserRequest(userRequest, user);

        return userMapper.userToUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "users", key = "#id")
    public void deleteById(Long id) {
        if (userRepository.existsById(id)) {
                throw new UserNotFoundException(ExceptionMessageGenerator.userNotFound(id));
        }

        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findAll(PageableFilter pageableFilter) {
        List<User> users = userRepository.findAll(
                PageRequest.of(pageableFilter.getPage(), pageableFilter.getSize())).getContent();

        return userMapper.usersToUsersResponseWithoutCards(users);
    }

}

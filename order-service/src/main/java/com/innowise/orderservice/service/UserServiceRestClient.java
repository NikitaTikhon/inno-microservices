package com.innowise.orderservice.service;

import com.innowise.orderservice.model.dto.UserResponse;

import java.util.List;
import java.util.Set;

/**
 * Service interface for REST client communication with the User Service.
 * Defines the contract for retrieving user information from a remote service.
 */
public interface UserServiceRestClient {

    /**
     * Retrieves user information by user ID from the User Service.
     *
     * @param userId The ID of the user to retrieve.
     * @return The {@link UserResponse} containing user information.
     * @throws com.innowise.orderservice.exception.ResourceNotFoundException if the user is not found.
     */
    UserResponse findUserById(Long userId);

    /**
     * Retrieves multiple users by their IDs from the User Service.
     *
     * @param userIds The set of user IDs to retrieve.
     * @return A {@link List} of {@link UserResponse} containing user information.
     */
    List<UserResponse> findUsersByIds(Set<Long> userIds);

}

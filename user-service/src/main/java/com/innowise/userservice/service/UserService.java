package com.innowise.userservice.service;

import com.innowise.userservice.model.dto.UserRequest;
import com.innowise.userservice.model.dto.UserResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for managing user-related business logic.
 * Defines the contract for CRUD operations on User entities.
 */
public interface UserService {

    /**
     * Saves a new User based on the provided DTO.
     *
     * @param userRequest The DTO containing the user's data.
     * @return The DTO of the newly created User.
     * @throws com.innowise.userservice.exception.UserAlreadyExistException if the user with the given email already exist.
     */
    UserResponse save(UserRequest userRequest);

    /**
     * Finds a User by their unique ID with CardInfo.
     *
     * @param id The ID of the user to find.
     * @return The DTO of the found User.
     * @throws com.innowise.userservice.exception.UserNotFoundException if the user with the given ID is not found.
     */
    UserResponse findById(Long id);

    /**
     * Finds a list of Users by a list of their IDs.
     *
     * @param ids A list of IDs of the users to find.
     * @return A list of DTOs for the found Users.
     */
    List<UserResponse> findByIds(List<Long> ids);

    /**
     * Finds a User by their email address.
     *
     * @param email The email address of the user to find.
     * @return The DTO of the found User.
     * @throws com.innowise.userservice.exception.UserNotFoundException if the user with the given email is not found.
     */
    UserResponse findByEmail(String email);

    /**
     * Updates an existing User based on the provided DTO and ID.
     *
     * @param userRequest The DTO containing the updated user data.
     * @param id The ID of the user to update.
     * @return The DTO of the updated User.
     * @throws com.innowise.userservice.exception.UserNotFoundException if the user with the given ID is not found.
     */
    UserResponse updateById(Long id, UserRequest userRequest);

    /**
     * Deletes a User by their unique ID.
     *
     * @param id The ID of the user to delete.
     * @throws com.innowise.userservice.exception.UserNotFoundException if the user with the given ID is not found.
     */
    void deleteById(Long id);


    /**
     * Finds a list of Users.
     *
     * @param pageable specifies the pagination (page number, page size) and
     * sorting criteria for the query.
     * @return A list of DTOs for the found Users.
     */
    List<UserResponse> findAll(Pageable pageable);

}

package com.innowise.authenticationservice.service;

import com.innowise.authenticationservice.model.dto.RegistrationRequest;
import com.innowise.authenticationservice.model.dto.UserDto;

/**
 * Service interface for managing user-related business logic.
 * Defines the contract for user registration and authentication operations.
 */
public interface UserService {

    /**
     * Saves a new User based on the provided authentication request.
     * The password is hashed using BCrypt before storing.
     *
     * @param registrationRequest The DTO containing the registration info.
     * @return The DTO of the newly created User.
     * @throws com.innowise.authenticationservice.exception.ResourceAlreadyExistsException if the user with the given email already exists.
     * @throws com.innowise.authenticationservice.exception.ResourceNotFoundException if the default role is not found.
     */
    UserDto save(RegistrationRequest registrationRequest);

}

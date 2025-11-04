package com.innowise.authenticationservice.service;

import com.innowise.authenticationservice.model.dto.UserDto;

/**
 * REST client interface for communication with the User Service.
 * <p>
 * This client handles inter-service communication between authentication-service and user-service.
 * All methods use internal API key authentication for secure service-to-service calls.
 * <p>
 * Implementation should include:
 * <ul>
 *   <li>Circuit breaker pattern for resilience</li>
 *   <li>Proper error handling and retries</li>
 *   <li>Timeouts configuration</li>
 * </ul>
 */
public interface UserServiceRestClient {

    /**
     * Creates a new user in the User Service.
     * <p>
     * This method makes a REST call to user-service to create user profile data.
     * The returned user includes the generated ID which must be synchronized with
     * authentication-service database.
     * <p>
     * This operation is protected by a circuit breaker to prevent cascading failures.
     *
     * @param userDto The user data to create (without ID).
     * @return The created user with generated ID and all fields populated.
     * @throws com.innowise.authenticationservice.exception.ExternalServiceException
     *         if the user-service is unavailable or returns an error.
     * @throws org.springframework.web.client.ResourceAccessException
     *         if connection to user-service fails.
     */
    UserDto save(UserDto userDto);

    /**
     * Deletes a user from the User Service by their ID.
     * <p>
     * This method is used as a compensating transaction when user creation in
     * authentication-service fails after successful creation in user-service.
     * It ensures data consistency between the two services.
     * <p>
     * This operation is protected by a circuit breaker and should be idempotent.
     *
     * @param id The ID of the user to delete.
     * @throws com.innowise.authenticationservice.exception.ExternalServiceException
     *         if the user-service is unavailable or returns an error.
     * @throws org.springframework.web.client.ResourceAccessException
     *         if connection to user-service fails.
     */
    void deleteById(Long id);

}

package com.innowise.userservice.repository;

import com.innowise.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link User} entities.
 * Provides standard CRUD operations and custom query methods.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their email address.
     * This is a named query method.
     *
     * @param email The email address to search for.
     * @return An {@link Optional} containing the found user, or empty if not found.
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds a list of users by a given list of IDs using a JPQL query.
     *
     * @param ids A list of user IDs to search for.
     * @return A list of found users.
     */
    @Query("SELECT u FROM User u WHERE u.id IN :ids")
    List<User> findByIdIn(List<Long> ids);

    /**
     * Deletes a user by ID using a native SQL query.
     * Note: This method is less preferred than using the built-in JpaRepository.deleteById()
     * because the latter is more portable and automatically handles cascading.
     *
     * @param id The ID of the user to delete.
     */
    @Modifying
    @Query(value = "DELETE FROM users AS u WHERE u.id = :id", nativeQuery = true)
    void deleteByIdNative(Long id);


    /**
     * Checks whether a user with the specified email exists.
     *
     * @param email the email address to check for existence
     * @return {@code true} if a user with the given email exists, {@code false} otherwise
     */
    boolean existsByEmail(String email);

}

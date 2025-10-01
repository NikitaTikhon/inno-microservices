package com.innowise.userservice.repository;


import com.innowise.userservice.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
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
     * Saves a given user.
     *
     * @param user The user to save.
     * @return The saved user.
     */
    @Override
    User save(User user);

    /**
     * Retrieves a user by its ID.
     *
     * @param id The ID of the user to retrieve.
     * @return An {@link Optional} containing the user entity with the given ID, or {@link Optional#empty()} if not found.
     */
    @Override
    Optional<User> findById(Long id);

    /**
     * Deletes the user with the given ID.
     *
     * @param id The ID of the user to delete.
     */
    @Override
    void deleteById(Long id);

    /**
     * Retrieves a user and his cards by its ID.
     *
     * @param id The ID of the user to retrieve.
     * @return An {@link Optional} containing the user entity with the given ID, or {@link Optional#empty()} if not found.
     */
    @Query("""
            SELECT u FROM User u
            LEFT JOIN FETCH u.cardsInfo
            WHERE u.id = :id
            """)
    Optional<User> findByIdWithCards(Long id);

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
     * Checks whether a user with the specified email exists.
     *
     * @param email the email address to check for existence
     * @return {@code true} if a user with the given email exists, {@code false} otherwise
     */
    boolean existsByEmail(String email);

}

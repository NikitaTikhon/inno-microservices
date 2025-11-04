package com.innowise.authenticationservice.repository;

import com.innowise.authenticationservice.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
     * Saves a user with an explicitly provided ID.
     *
     * @param user The user entity with the ID already set.
     */
    @Modifying
    @Query(value = """
        INSERT INTO users (id, email, password, creation_date)
        VALUES (:#{#user.id}, :#{#user.email}, :#{#user.password}, CURRENT_DATE)
        """, nativeQuery = true)
    void saveWithExplicitId(User user);

    /**
     * Creates a relationship between a user and a role.
     *
     * @param userId The ID of the user.
     * @param roleId The ID of the role to assign.
     */
    @Modifying
    @Query(value = """
        INSERT INTO users_roles (user_id, role_id)
        VALUES (:userId, :roleId)
        """, nativeQuery = true)
    void saveUserRole(Long userId, Long roleId);

    /**
     * Finds a user by their email address.
     * This is a named query method.
     *
     * @param email The email address to search for.
     * @return An {@link Optional} containing the found user, or empty if not found.
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks whether a user with the specified email exists.
     *
     * @param email the email address to check for existence
     * @return {@code true} if a user with the given email exists, {@code false} otherwise
     */
    boolean existsByEmail(String email);

}

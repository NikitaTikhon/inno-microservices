package com.innowise.authenticationservice.repository;

import com.innowise.authenticationservice.model.RoleEnum;
import com.innowise.authenticationservice.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing {@link Role} entities.
 * Provides standard CRUD operations and custom query methods.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Finds a role by its name.
     * This is a named query method.
     *
     * @param roleEnum The role name to search for.
     * @return An {@link Optional} containing the found role, or empty if not found.
     */
    Optional<Role> findByName(RoleEnum roleEnum);

}

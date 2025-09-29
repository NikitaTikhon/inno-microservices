package com.innowise.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

/**
 * Repository interface.
 * Provides standard CRUD operations.
 */
@NoRepositoryBean
public interface CrudRepository<T, ID> extends JpaRepository<T, ID> {

    @Override
    <S extends T> S save(S entity);

    @Override
    Optional<T> findById(ID id);

    @Override
    void deleteById(ID id);

}

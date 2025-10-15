package com.innowise.orderservice.repository;

import com.innowise.orderservice.model.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing {@link Item} entities.
 * Provides standard CRUD operations and custom query methods.
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    /**
     * Finds items by a list of IDs.
     * This is a named query method.
     *
     * @param ids The list of item IDs to search for.
     * @return A {@link List} of items with the specified IDs.
     */
    List<Item> findByIdIn(List<Long> ids);

}

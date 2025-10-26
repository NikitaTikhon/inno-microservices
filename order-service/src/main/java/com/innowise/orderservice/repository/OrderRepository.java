package com.innowise.orderservice.repository;

import com.innowise.orderservice.model.entity.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link Order} entities.
 * Provides standard CRUD operations and custom query methods with entity graph support.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Saves a given order.
     *
     * @param order The order to save.
     * @return The saved order.
     */
    @Override
    Order save(Order order);

    /**
     * Checks whether an order with the specified ID exists.
     *
     * @param id The order ID to check for existence.
     * @return {@code true} if an order with the given ID exists, {@code false} otherwise.
     */
    @Override
    boolean existsById(Long id);

    /**
     * Deletes the order with the specified ID.
     *
     * @param id The ID of the order to delete.
     */
    @Override
    void deleteById(Long id);

    /**
     * Finds an order by its ID with eagerly loaded order items and associated items.
     * Uses {@link EntityGraph} to optimize fetching and avoid N+1 query problem.
     *
     * @param id The order ID to search for.
     * @return An {@link Optional} containing the found order with its items, or empty if not found.
     */
    @Override
    @EntityGraph(attributePaths = {"orderItems", "orderItems.item"})
    Optional<Order> findById(Long id);

    /**
     * Finds an order by its ID and user ID with eagerly loaded order items and associated items.
     * Uses {@link EntityGraph} to optimize fetching and avoid N+1 query problem.
     *
     * @param id The order ID to search for.
     * @param userId The user ID to search for.
     * @return An {@link Optional} containing the found order with its items, or empty if not found.
     */
    @EntityGraph(attributePaths = {"orderItems", "orderItems.item"})
    Optional<Order> findByIdAndUserId(Long id, Long userId);

    /**
     * Finds all orders matching the given specification with pagination support.
     * Uses {@link EntityGraph} to eagerly load order items and associated items.
     *
     * @param spec The specification to filter orders.
     * @param pageable The pagination information.
     * @return A {@link List} of orders matching the specification.
     */
    @EntityGraph(attributePaths = {"orderItems", "orderItems.item"})
    List<Order> findAll(Specification<Order> spec, Pageable pageable);

}

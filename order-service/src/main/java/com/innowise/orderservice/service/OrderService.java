package com.innowise.orderservice.service;

import com.innowise.orderservice.model.dto.FilterRequest;
import com.innowise.orderservice.model.dto.OrderRequest;
import com.innowise.orderservice.model.dto.OrderResponse;
import com.innowise.orderservice.model.dto.PageableRequest;

import java.util.List;

/**
 * Service interface for managing order-related business logic.
 * Defines the contract for order CRUD operations with user information integration.
 */
public interface OrderService {

    /**
     * Creates a new order for the specified user.
     * Validates order items and retrieves user information from the User Service.
     *
     * @param userId The ID of the user creating the order.
     * @param orderRequest The DTO containing order details.
     * @return The {@link OrderResponse} containing the created order with user information.
     * @throws com.innowise.orderservice.exception.ResourceNotFoundException if the user or items are not found.
     */
    OrderResponse save(Long userId, OrderRequest orderRequest);

    /**
     * Retrieves an order by its ID for the specified user.
     * Includes user information from the User Service.
     *
     * @param id The ID of the order to retrieve.
     * @param userId The ID of the user requesting the order.
     * @return The {@link OrderResponse} containing the order with user information.
     * @throws com.innowise.orderservice.exception.ResourceNotFoundException if the order or user is not found.
     */
    OrderResponse findByIdAndUserId(Long id, Long userId);

    /**
     * Retrieves a filtered and paginated list of orders.
     * Supports filtering by order IDs and statuses, includes user information for all orders.
     *
     * @param filterRequest The DTO containing filter criteria (IDs and statuses).
     * @param pageableRequest The DTO containing pagination parameters.
     * @return A {@link List} of {@link OrderResponse} containing filtered orders with user information.
     */
    List<OrderResponse> findByFilter(FilterRequest filterRequest, PageableRequest pageableRequest);

    /**
     * Updates an existing order by its ID.
     * Updates order status and order items, includes user information in the response.
     *
     * @param id The ID of the order to update.
     * @param orderRequest The DTO containing updated order details.
     * @return The {@link OrderResponse} containing the updated order with user information.
     * @throws com.innowise.orderservice.exception.ResourceNotFoundException if the order or items are not found.
     */
    OrderResponse updateById(Long id, OrderRequest orderRequest);

    /**
     * Deletes an order by its ID.
     *
     * @param id The ID of the order to delete.
     * @throws com.innowise.orderservice.exception.ResourceNotFoundException if the order is not found.
     */
    void deleteById(Long id);

}

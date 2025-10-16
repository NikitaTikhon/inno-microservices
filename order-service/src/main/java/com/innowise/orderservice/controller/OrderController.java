package com.innowise.orderservice.controller;

import com.innowise.orderservice.model.AuthUser;
import com.innowise.orderservice.model.dto.FilterRequest;
import com.innowise.orderservice.model.dto.OrderRequest;
import com.innowise.orderservice.model.dto.OrderResponse;
import com.innowise.orderservice.model.dto.PageableRequest;
import com.innowise.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing orders.
 * Provides endpoints for creating, reading, updating, and deleting orders.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    /**
     * Creates a new order for the authenticated user.
     *
     * @param authUser the authenticated user
     * @param orderRequest the order details
     * @return ResponseEntity containing the created order
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<OrderResponse> save(@AuthenticationPrincipal AuthUser authUser, @RequestBody @Valid OrderRequest orderRequest) {
        OrderResponse order = orderService.save(authUser.getId(), orderRequest);

        return ResponseEntity.ok(order);
    }

    /**
     * Retrieves an order by ID for the authenticated user.
     *
     * @param authUser the authenticated user
     * @param id the order ID
     * @return ResponseEntity containing the order details
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<OrderResponse> findByIdAndUserId(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        OrderResponse order = orderService.findByIdAndUserId(id, authUser.getId());

        return ResponseEntity.ok(order);
    }

    /**
     * Retrieves a list of orders based on filter criteria.
     * This endpoint is restricted to administrators only.
     *
     * @param filterRequest the filter criteria
     * @param pageableRequest the pagination parameters
     * @return ResponseEntity containing the list of orders
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> findByFilter(FilterRequest filterRequest, @Valid PageableRequest pageableRequest) {
        List<OrderResponse> orders = orderService.findByFilter(filterRequest, pageableRequest);

        return ResponseEntity.ok(orders);
    }

    /**
     * Updates an existing order by ID.
     * This endpoint is restricted to administrators only.
     *
     * @param id the order ID
     * @param orderRequest the updated order details
     * @return ResponseEntity containing the updated order
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateById(@PathVariable Long id, @RequestBody @Valid OrderRequest orderRequest) {
        OrderResponse order = orderService.updateById(id, orderRequest);

        return ResponseEntity.ok(order);
    }

    /**
     * Deletes an order by ID.
     * This endpoint is restricted to administrators only.
     *
     * @param id the order ID
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        orderService.deleteById(id);

        return ResponseEntity.noContent().build();
    }

}

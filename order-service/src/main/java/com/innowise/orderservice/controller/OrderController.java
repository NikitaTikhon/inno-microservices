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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> save(@AuthenticationPrincipal AuthUser authUser, @RequestBody @Valid OrderRequest orderRequest) {
        OrderResponse order = orderService.save(authUser.getId(), orderRequest);

        return ResponseEntity.ok(order);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> findById(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        OrderResponse order = orderService.findById(authUser.getId(), id);

        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> findByFilter(FilterRequest filterRequest, @Valid PageableRequest pageableRequest) {
        List<OrderResponse> orders = orderService.findByFilter(filterRequest, pageableRequest);

        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> updateById(@PathVariable Long id, @RequestBody @Valid OrderRequest orderRequest) {
        OrderResponse order = orderService.updateById(id, orderRequest);

        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        orderService.deleteById(id);

        return ResponseEntity.noContent().build();
    }

}

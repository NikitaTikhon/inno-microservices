package com.innowise.orderservice.util;

import com.innowise.orderservice.model.OrderStatus;
import com.innowise.orderservice.model.dto.OrderItemRequest;
import com.innowise.orderservice.model.dto.OrderRequest;
import com.innowise.orderservice.model.dto.OrderResponse;
import com.innowise.orderservice.model.dto.UserResponse;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public final class OrderUtil {

    private OrderUtil() {}

    public static UserResponse createUserResponse(Long userId) {
        return UserResponse.builder()
                .id(userId)
                .email("user" + userId + "@example.com")
                .build();
    }

    public static Item createItem(Long itemId, String name, BigDecimal price) {
        return Item.builder()
                .id(itemId)
                .name(name)
                .price(price)
                .build();
    }

    public static List<Item> createItems() {
        return List.of(
                createItem(1L, "Item 1", BigDecimal.valueOf(10.00)),
                createItem(2L, "Item 2", BigDecimal.valueOf(20.00)),
                createItem(3L, "Item 3", BigDecimal.valueOf(30.00))
        );
    }

    public static OrderRequest createOrderRequest(OrderStatus status, List<OrderItemRequest> orderItems) {
        return OrderRequest.builder()
                .status(status)
                .orderItems(orderItems)
                .build();
    }

    public static OrderRequest createOrderRequest(OrderStatus status) {
        return createOrderRequest(status, createOrderItemsRequest());
    }

    public static List<OrderItemRequest> createOrderItemsRequest() {
        return List.of(
                new OrderItemRequest(1L, 1L),
                new OrderItemRequest(2L, 5L),
                new OrderItemRequest(3L, 2L)
        );
    }

    public static Order createOrder(Long orderId, Long userId, OrderStatus status) {
        return Order.builder()
                .id(orderId)
                .userId(userId)
                .status(status)
                .build();
    }

    public static Order createOrderWithItems(Long orderId, Long userId, OrderStatus status, List<Item> items) {
        Order order = createOrder(orderId, userId, status);
        List<OrderItem> orderItems = new ArrayList<>();

        for (Item item : items) {
            OrderItem orderItem = OrderItem.builder()
                    .id(orderItems.size() + 1L)
                    .order(order)
                    .item(item)
                    .quantity(1L)
                    .build();
            orderItems.add(orderItem);
        }

        order.setOrderItems(orderItems);
        return order;
    }

    public static OrderResponse createOrderResponse(Long orderId, Long userId, OrderStatus status, UserResponse user) {
        return OrderResponse.builder()
                .id(orderId)
                .userId(userId)
                .status(status)
                .user(user)
                .build();
    }

    public static OrderItem createOrderItem(Order order, Item item, Long quantity) {
        return OrderItem.builder()
                .order(order)
                .item(item)
                .quantity(quantity)
                .build();
    }

}

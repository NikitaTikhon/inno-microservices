package com.innowise.orderservice.mapper;

import com.innowise.orderservice.model.dto.OrderResponse;
import com.innowise.orderservice.model.dto.UserResponse;
import com.innowise.orderservice.model.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;

/**
 * Mapper interface for converting {@link Order} entities to {@link OrderResponse} DTOs.
 * Uses MapStruct for automatic mapping generation and {@link OrderItemMapper} for nested mappings.
 */
@Mapper(componentModel = "spring", uses = OrderItemMapper.class)
public interface OrderMapper {

    /**
     * Converts an {@link Order} entity to an {@link OrderResponse} DTO.
     * The user field is ignored and should be set separately.
     *
     * @param order The order entity to convert.
     * @return The converted order response DTO.
     */
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "orderItems", source = "orderItems")
    OrderResponse orderToOrderResponse(Order order);

    /**
     * Converts an {@link Order} entity to an {@link OrderResponse} DTO with user information.
     *
     * @param order The order entity to convert.
     * @param userResponse The user response DTO to include in the order response.
     * @return The converted order response DTO with user information.
     */
    default OrderResponse orderToOrderResponse(Order order, UserResponse userResponse) {
        OrderResponse response = orderToOrderResponse(order);
        response.setUser(userResponse);

        return response;
    }

    /**
     * Converts a list of {@link Order} entities to a list of {@link OrderResponse} DTOs
     * with corresponding user information.
     *
     * @param orders The list of order entities to convert.
     * @param usersResponse A map of user IDs to user response DTOs.
     * @return The list of converted order response DTOs with user information.
     */
    default List<OrderResponse> ordersToOrdersResponse(List<Order> orders, Map<Long, UserResponse> usersResponse) {
        return orders.stream()
                .map(order -> orderToOrderResponse(order, usersResponse.get(order.getUserId())))
                .toList();
    }

}

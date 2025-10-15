package com.innowise.orderservice.mapper;

import com.innowise.orderservice.model.dto.OrderItemResponse;
import com.innowise.orderservice.model.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper interface for converting {@link OrderItem} entities to {@link OrderItemResponse} DTOs.
 * Uses MapStruct for automatic mapping generation with custom field mappings.
 */
@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    /**
     * Converts an {@link OrderItem} entity to an {@link OrderItemResponse} DTO.
     * Maps nested order ID, item ID, and item price to the response.
     *
     * @param orderItem The order item entity to convert.
     * @return The converted order item response DTO.
     */
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "price", source = "item.price")
    OrderItemResponse orderItemToOrderItemResponse(OrderItem orderItem);

    /**
     * Converts a list of {@link OrderItem} entities to a list of {@link OrderItemResponse} DTOs.
     *
     * @param orderItems The list of order item entities to convert.
     * @return The list of converted order item response DTOs.
     */
    List<OrderItemResponse> orderItemsToOrderItemsResponse(List<OrderItem> orderItems);

}

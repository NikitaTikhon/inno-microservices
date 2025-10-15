package com.innowise.orderservice.model.dto;

import com.innowise.orderservice.model.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {

    private OrderStatus status = OrderStatus.NEW;

    @Valid
    @NotEmpty(message = "Order item list cannot be empty")
    private List<OrderItemRequest> orderItems;

}

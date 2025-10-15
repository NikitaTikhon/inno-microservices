package com.innowise.orderservice.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemRequest {

    @NotNull(message = "Item id cannot be blank")
    private Long itemId;

    @NotNull(message = "Quantity cannot be blank")
    @Min(value = 1, message = "Quantity must be greater than or equal to 1")
    private Long quantity;

}

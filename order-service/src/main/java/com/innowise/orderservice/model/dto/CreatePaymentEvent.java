package com.innowise.orderservice.model.dto;

import com.innowise.orderservice.model.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class CreatePaymentEvent {

    @NotNull(message = "Order id cannot be null")
    @Positive(message = "Order id must be positive")
    private Long orderId;

    @NotNull(message = "Payment status cannot be null")
    private PaymentStatus status;

}

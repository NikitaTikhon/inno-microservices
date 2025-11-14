package com.innowise.paymentservice.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull(message = "Order id cannot be null")
    @Positive(message = "Order id must be positive")
    private Long orderId;

    @NotNull(message = "User id cannot be null")
    @Positive(message = "User id must be positive")
    private Long userId;

    @NotNull(message = "Payment amount cannot be null")
    @Positive(message = "Payment amount must be positive")
    private BigDecimal paymentAmount;

}

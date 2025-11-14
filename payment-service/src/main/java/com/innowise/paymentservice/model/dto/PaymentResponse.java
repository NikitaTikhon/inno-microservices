package com.innowise.paymentservice.model.dto;

import com.innowise.paymentservice.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private String id;
    private Long orderId;
    private Long userId;
    private PaymentStatus status;
    private LocalDateTime timestamp;
    private BigDecimal paymentAmount;

}

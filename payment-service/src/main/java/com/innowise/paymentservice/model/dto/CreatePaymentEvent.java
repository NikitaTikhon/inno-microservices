package com.innowise.paymentservice.model.dto;

import com.innowise.paymentservice.model.PaymentStatus;
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

    private Long orderId;
    private PaymentStatus status;

}

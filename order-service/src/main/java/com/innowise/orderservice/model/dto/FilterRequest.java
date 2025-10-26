package com.innowise.orderservice.model.dto;

import com.innowise.orderservice.model.OrderStatus;
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
public class FilterRequest {

    private List<Long> ids;

    private List<OrderStatus> statuses;

}

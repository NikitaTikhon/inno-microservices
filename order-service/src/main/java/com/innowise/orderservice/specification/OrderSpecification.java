package com.innowise.orderservice.specification;

import com.innowise.orderservice.model.OrderStatus;
import com.innowise.orderservice.model.entity.Order;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class OrderSpecification {

    private OrderSpecification() {}

    public static Specification<Order> idIn(List<Long> ids) {
        return (root, query, cb) -> root.get("id").in(ids);
    }

    public static Specification<Order> statusIn(List<OrderStatus> statuses) {
        return (root, query, cb) -> root.get("status").in(statuses);
    }

}

package com.innowise.orderservice.integration.repository;

import com.innowise.orderservice.integration.BaseIntegrationRepositoryTest;
import com.innowise.orderservice.model.OrderStatus;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderItem;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.innowise.orderservice.util.OrderUtil.createItem;
import static com.innowise.orderservice.util.OrderUtil.createOrder;
import static com.innowise.orderservice.util.OrderUtil.createOrderItem;
import static org.assertj.core.api.Assertions.assertThat;

class OrderRepositoryIT extends BaseIntegrationRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Item item1;
    private Item item2;
    private Item item3;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        itemRepository.deleteAll();
        entityManager.flush();

        item1 = itemRepository.save(createItem(null, "Item 1", BigDecimal.valueOf(100.00)));
        item2 = itemRepository.save(createItem(null, "Item 2", BigDecimal.valueOf(200.00)));
        item3 = itemRepository.save(createItem(null, "Item 3", BigDecimal.valueOf(300.00)));
    }

    @Test
    @DisplayName("Should save order with order items successfully")
    void save_ShouldSaveOrder_WhenValidOrder() {
        Order order = createOrder(null, 1L, OrderStatus.NEW);

        OrderItem orderItem1 = createOrderItem(order, item1, 2L);
        OrderItem orderItem2 = createOrderItem(order, item2, 3L);

        order.setOrderItems(List.of(orderItem1, orderItem2));

        Order savedOrder = orderRepository.save(order);

        assertThat(savedOrder).isNotNull();
        assertThat(savedOrder.getId()).isNotNull();
        assertThat(savedOrder.getUserId()).isEqualTo(1L);
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(savedOrder.getOrderItems()).hasSize(2);
    }

    @Test
    @DisplayName("Should find order by id with entity graph")
    void findById_ShouldReturnOrderWithItems_WhenOrderExists() {
        Order order = createOrder(null, 1L, OrderStatus.NEW);
        OrderItem orderItem = createOrderItem(order, item1, 1L);
        order.setOrderItems(List.of(orderItem));

        Order savedOrder = orderRepository.save(order);
        entityManager.clear();

        Optional<Order> foundOrder = orderRepository.findById(savedOrder.getId());

        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getId()).isEqualTo(savedOrder.getId());
        assertThat(foundOrder.get().getOrderItems()).isNotEmpty();
        assertThat(foundOrder.get().getOrderItems()).hasSize(1);
        assertThat(foundOrder.get().getOrderItems().getFirst().getItem().getName()).isEqualTo("Item 1");
    }

    @Test
    @DisplayName("Should find order by id and userId successfully")
    void findByIdAndUserId_ShouldReturnOrder_WhenOrderExists() {
        Long userId = 1L;
        Order order = createOrder(null, userId, OrderStatus.NEW);
        OrderItem orderItem = createOrderItem(order, item1, 1L);
        order.setOrderItems(List.of(orderItem));

        Order savedOrder = orderRepository.save(order);
        entityManager.clear();

        Optional<Order> foundOrder = orderRepository.findByIdAndUserId(savedOrder.getId(), userId);

        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getId()).isEqualTo(savedOrder.getId());
        assertThat(foundOrder.get().getUserId()).isEqualTo(userId);
        assertThat(foundOrder.get().getOrderItems()).hasSize(1);
    }

    @Test
    @DisplayName("Should not find order with different userId")
    void findByIdAndUserId_ShouldReturnEmpty_WhenDifferentUserId() {
        Order order = createOrder(null, 1L, OrderStatus.NEW);
        OrderItem orderItem = createOrderItem(order, item1, 1L);
        order.setOrderItems(List.of(orderItem));

        Order savedOrder = orderRepository.save(order);

        Optional<Order> foundOrder = orderRepository.findByIdAndUserId(savedOrder.getId(), 2L);

        assertThat(foundOrder).isEmpty();
    }

    @Test
    @DisplayName("Should find all orders with specification and pagination")
    void findAll_ShouldReturnOrders_WhenSpecificationMatches() {
        Order order1 = createOrder(null, 1L, OrderStatus.NEW);
        order1.setOrderItems(List.of(createOrderItem(order1, item1, 1L)));

        Order order2 = createOrder(null, 2L, OrderStatus.PREPARED);
        order2.setOrderItems(List.of(createOrderItem(order2, item2, 1L)));

        Order order3 = createOrder(null, 1L, OrderStatus.NEW);
        order3.setOrderItems(List.of(createOrderItem(order3, item3, 1L)));

        orderRepository.save(order1);
        orderRepository.save(order2);
        orderRepository.save(order3);

        Specification<Order> spec = (root, query, cb) -> cb.equal(root.get("status"), OrderStatus.NEW);
        PageRequest pageRequest = PageRequest.of(0, 10);

        List<Order> foundOrders = orderRepository.findAll(spec, pageRequest);

        assertThat(foundOrders).hasSize(2)
                .allMatch(order -> order.getStatus() == OrderStatus.NEW);
    }

    @Test
    @DisplayName("Should delete order by id successfully")
    void deleteById_ShouldDeleteOrder_WhenOrderExists() {
        Order order = createOrder(null, 1L, OrderStatus.NEW);
        OrderItem orderItem = createOrderItem(order, item1, 1L);
        order.setOrderItems(List.of(orderItem));

        Order savedOrder = orderRepository.save(order);
        Long orderId = savedOrder.getId();

        orderRepository.deleteById(orderId);
        entityManager.flush();

        Optional<Order> deletedOrder = orderRepository.findById(orderId);
        assertThat(deletedOrder).isEmpty();
    }

}

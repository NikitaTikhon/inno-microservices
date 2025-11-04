package com.innowise.orderservice.integration.service;

import com.innowise.orderservice.exception.ResourceNotFoundException;
import com.innowise.orderservice.integration.BaseIntegrationTest;
import com.innowise.orderservice.model.OrderStatus;
import com.innowise.orderservice.model.dto.FilterRequest;
import com.innowise.orderservice.model.dto.OrderItemRequest;
import com.innowise.orderservice.model.dto.OrderRequest;
import com.innowise.orderservice.model.dto.OrderResponse;
import com.innowise.orderservice.model.dto.PageableRequest;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.service.OrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.reset;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.innowise.orderservice.util.OrderUtil.createItem;
import static com.innowise.orderservice.util.OrderUtil.createOrderRequest;
import static com.innowise.orderservice.util.SecurityUtil.clearAuthentication;
import static com.innowise.orderservice.util.SecurityUtil.setupAuthentication;
import static com.innowise.orderservice.util.WireMockStubUtil.createUserResponse;
import static com.innowise.orderservice.util.WireMockStubUtil.stubUserServiceFindById;
import static com.innowise.orderservice.util.WireMockStubUtil.stubUserServiceFindByIds;
import static com.innowise.orderservice.util.WireMockStubUtil.stubUserServiceNotFound;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class OrderServiceIT extends BaseIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ItemRepository itemRepository;

    private Item item1;
    private Item item2;
    private Item item3;

    @BeforeEach
    void setUp() {
        setupAuthentication(1L);
        orderRepository.deleteAll();
        itemRepository.deleteAll();
        reset();

        item1 = itemRepository.save(createItem(null, "Test Item 1", BigDecimal.valueOf(100.00)));
        item2 = itemRepository.save(createItem(null, "Test Item 2", BigDecimal.valueOf(200.00)));
        item3 = itemRepository.save(createItem(null, "Test Item 3", BigDecimal.valueOf(300.00)));

        stubUserServiceFindById(1L, "User 1", "user1@test.com");
        stubUserServiceFindById(2L, "User 2", "user2@test.com");
    }

    @AfterEach
    void shutDown() {
        clearAuthentication();
    }

    @Test
    @DisplayName("Should save order with items to database successfully")
    void save_ShouldSaveOrderWithItems_WhenValidRequest() {
        Long userId = 1L;
        OrderRequest orderRequest = createOrderRequest(OrderStatus.NEW, List.of(
                new OrderItemRequest(item1.getId(), 2L),
                new OrderItemRequest(item2.getId(), 1L),
                new OrderItemRequest(item3.getId(), 3L)
        ));

        OrderResponse response = orderService.save(userId, orderRequest);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getId()).isEqualTo(userId);

        Optional<Order> savedOrder = orderRepository.findById(response.getId());
        assertThat(savedOrder).isPresent();
        assertThat(savedOrder.get().getUserId()).isEqualTo(userId);
        assertThat(savedOrder.get().getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(savedOrder.get().getOrderItems()).hasSize(3);

        assertThat(savedOrder.get().getOrderItems())
                .extracting(orderItem -> orderItem.getItem().getName())
                .containsExactlyInAnyOrder("Test Item 1", "Test Item 2", "Test Item 3");

        assertThat(savedOrder.get().getOrderItems())
                .anyMatch(oi -> oi.getItem().getId().equals(item1.getId()) && oi.getQuantity().equals(2L));
        assertThat(savedOrder.get().getOrderItems())
                .anyMatch(oi -> oi.getItem().getId().equals(item2.getId()) && oi.getQuantity().equals(1L));
        assertThat(savedOrder.get().getOrderItems())
                .anyMatch(oi -> oi.getItem().getId().equals(item3.getId()) && oi.getQuantity().equals(3L));
    }

    @Test
    @DisplayName("Should throw exception when saving order with non-existent items")
    void save_ShouldThrowException_WhenItemsNotFound() {
        Long userId = 1L;
        OrderRequest orderRequest = createOrderRequest(OrderStatus.NEW, List.of(
                        new OrderItemRequest(999L, 1L)
        ));

        assertThatThrownBy(() -> orderService.save(userId, orderRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should find order by id and userId successfully")
    void findByIdAndUserId_ShouldReturnOrder_WhenOrderExists() {
        Long userId = 1L;
        OrderRequest orderRequest = createOrderRequest(OrderStatus.NEW, List.of(
                new OrderItemRequest(item1.getId(), 1L)
        ));

        OrderResponse createdOrder = orderService.save(userId, orderRequest);

        OrderResponse foundOrder = orderService.findByIdAndUserId(createdOrder.getId(), userId);

        assertThat(foundOrder).isNotNull();
        assertThat(foundOrder.getId()).isEqualTo(createdOrder.getId());
        assertThat(foundOrder.getUserId()).isEqualTo(userId);
        assertThat(foundOrder.getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(foundOrder.getUser()).isNotNull();
    }

    @Test
    @DisplayName("Should throw exception when order not found by id and userId")
    void findByIdAndUserId_ShouldThrowException_WhenOrderNotFound() {
        Long orderId = 999L;
        Long userId = 1L;

        assertThatThrownBy(() -> orderService.findByIdAndUserId(orderId, userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should find orders by filter successfully")
    void findByFilter_ShouldReturnOrders_WhenFilterMatches() {
        Long userId1 = 1L;
        Long userId2 = 2L;

        OrderResponse order1 = orderService.save(userId1, createOrderRequest(OrderStatus.NEW, List.of(
                new OrderItemRequest(item1.getId(), 1L)
        )));

        OrderResponse order2 = orderService.save(userId2, createOrderRequest(OrderStatus.PREPARED, List.of(
                new OrderItemRequest(item2.getId(), 1L)
        )));

        orderService.save(userId1, createOrderRequest(OrderStatus.NEW, List.of(
                new OrderItemRequest(item3.getId(), 1L)
        )));

        stubUserServiceFindByIds(List.of(
                createUserResponse(1L, "User 1", "user1@test.com"),
                createUserResponse(2L, "User 2", "user2@test.com")
        ));

        FilterRequest filterRequest = FilterRequest.builder()
                .ids(List.of(order1.getId(), order2.getId()))
                .build();
        PageableRequest pageableRequest = new PageableRequest(10, 0);

        List<OrderResponse> foundOrders = orderService.findByFilter(filterRequest, pageableRequest);

        assertThat(foundOrders).isNotNull()
                .hasSize(2)
                .extracting(OrderResponse::getId)
                .containsExactlyInAnyOrder(order1.getId(), order2.getId());
    }

    @Test
    @DisplayName("Should find orders by status filter")
    void findByFilter_ShouldReturnOrders_WhenFilterByStatus() {
        Long userId1 = 1L;
        Long userId2 = 2L;

        orderService.save(userId1, createOrderRequest(OrderStatus.NEW, List.of(
                new OrderItemRequest(item1.getId(), 1L)
        )));

        orderService.save(userId2, createOrderRequest(OrderStatus.PREPARED, List.of(
                new OrderItemRequest(item2.getId(), 1L)
        )));

        orderService.save(userId1, createOrderRequest(OrderStatus.PREPARED, List.of(
                new OrderItemRequest(item3.getId(), 1L)
        )));

        stubUserServiceFindByIds(List.of(
                createUserResponse(1L, "User 1", "user1@test.com"),
                createUserResponse(2L, "User 2", "user2@test.com")
        ));

        FilterRequest filterRequest = FilterRequest.builder()
                .statuses(List.of(OrderStatus.PREPARED))
                .build();
        PageableRequest pageableRequest = new PageableRequest(10, 0);

        List<OrderResponse> foundOrders = orderService.findByFilter(filterRequest, pageableRequest);

        assertThat(foundOrders).isNotNull()
                .hasSize(2)
                .allMatch(order -> order.getStatus() == OrderStatus.PREPARED);
    }

    @Test
    @DisplayName("Should update order successfully")
    void updateById_ShouldUpdateOrder_WhenValidRequest() {
        Long userId = 1L;
        OrderRequest initialRequest = createOrderRequest(OrderStatus.NEW, List.of(
                new OrderItemRequest(item1.getId(), 1L),
                new OrderItemRequest(item2.getId(), 2L)
        ));

        OrderResponse createdOrder = orderService.save(userId, initialRequest);

        OrderRequest updateRequest = createOrderRequest(OrderStatus.PREPARED, List.of(
                new OrderItemRequest(item1.getId(), 3L),
                new OrderItemRequest(item3.getId(), 1L)
        ));

        OrderResponse updatedOrder = orderService.updateById(createdOrder.getId(), updateRequest);

        assertThat(updatedOrder).isNotNull();
        assertThat(updatedOrder.getId()).isEqualTo(createdOrder.getId());
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PREPARED);

        Optional<Order> savedOrder = orderRepository.findById(updatedOrder.getId());
        assertThat(savedOrder).isPresent();
        assertThat(savedOrder.get().getStatus()).isEqualTo(OrderStatus.PREPARED);
        assertThat(savedOrder.get().getOrderItems()).hasSize(2);
        assertThat(savedOrder.get().getOrderItems())
                .extracting(orderItem -> orderItem.getItem().getId())
                .containsExactlyInAnyOrder(item1.getId(), item3.getId());
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent order")
    void updateById_ShouldThrowException_WhenOrderNotFound() {
        Long orderId = 999L;
        OrderRequest updateRequest = createOrderRequest(OrderStatus.PREPARED);

        assertThatThrownBy(() -> orderService.updateById(orderId, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should delete order successfully")
    void deleteById_ShouldDeleteOrder_WhenOrderExists() {
        Long userId = 1L;
        OrderRequest orderRequest = createOrderRequest(OrderStatus.NEW, List.of(
                new OrderItemRequest(item1.getId(), 1L)
        ));

        OrderResponse createdOrder = orderService.save(userId, orderRequest);
        Long orderId = createdOrder.getId();

        orderService.deleteById(orderId);

        Optional<Order> deletedOrder = orderRepository.findById(orderId);
        assertThat(deletedOrder).isEmpty();
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent order")
    void deleteById_ShouldThrowException_WhenOrderNotFound() {
        Long orderId = 999L;

        assertThatThrownBy(() -> orderService.deleteById(orderId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found and verify HTTP call with JWT")
    void save_ShouldThrowException_WhenUserNotFound() {
        Long userId = 999L;
        OrderRequest orderRequest = createOrderRequest(OrderStatus.NEW, List.of(
                new OrderItemRequest(item1.getId(), 1L)
        ));

        stubUserServiceNotFound(userId);

        assertThatThrownBy(() -> orderService.save(userId, orderRequest))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(getRequestedFor(urlEqualTo("/users/" + userId))
                .withHeader("Authorization", matching("Bearer .*")));
    }

}

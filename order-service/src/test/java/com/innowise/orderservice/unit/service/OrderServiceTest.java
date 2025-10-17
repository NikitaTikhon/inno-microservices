package com.innowise.orderservice.unit.service;

import com.innowise.orderservice.exception.ResourceNotFoundException;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.model.OrderStatus;
import com.innowise.orderservice.model.dto.FilterRequest;
import com.innowise.orderservice.model.dto.OrderRequest;
import com.innowise.orderservice.model.dto.OrderResponse;
import com.innowise.orderservice.model.dto.PageableRequest;
import com.innowise.orderservice.model.dto.UserResponse;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.service.impl.OrderServiceImpl;
import com.innowise.orderservice.service.impl.UserServiceRestClientImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.innowise.orderservice.util.OrderUtil.createItems;
import static com.innowise.orderservice.util.OrderUtil.createOrder;
import static com.innowise.orderservice.util.OrderUtil.createOrderRequest;
import static com.innowise.orderservice.util.OrderUtil.createOrderResponse;
import static com.innowise.orderservice.util.OrderUtil.createOrderWithItems;
import static com.innowise.orderservice.util.OrderUtil.createUserResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock
    private UserServiceRestClientImpl userServiceRestClient;

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ItemRepository itemRepository;

    @Mock
    private OrderMapper orderMapper;

    @Test
    @DisplayName("Should create order successfully")
    void save_ShouldCreateOrder() {
        Long userId = 1L;
        OrderRequest orderRequest = createOrderRequest(OrderStatus.NEW);
        UserResponse userResponse = createUserResponse(userId);
        List<Item> items = createItems();
        Order savedOrder = createOrder(1L, userId, OrderStatus.NEW);
        OrderResponse expectedResponse = createOrderResponse(1L, userId, OrderStatus.NEW, userResponse);

        when(userServiceRestClient.findUserById(userId)).thenReturn(userResponse);
        when(itemRepository.findByIdIn(anyList())).thenReturn(items);
        when(orderRepository.save(any())).thenReturn(savedOrder);
        when(orderMapper.orderToOrderResponse(any(), any())).thenReturn(expectedResponse);

        OrderResponse actualResponse = orderService.save(userId, orderRequest);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getId()).isEqualTo(expectedResponse.getId());
        assertThat(actualResponse.getUserId()).isEqualTo(expectedResponse.getUserId());
        assertThat(actualResponse.getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(actualResponse.getUser()).isEqualTo(userResponse);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        
        Order capturedOrder = orderCaptor.getValue();
        assertThat(capturedOrder.getUserId()).isEqualTo(userId);
        assertThat(capturedOrder.getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(capturedOrder.getOrderItems()).hasSize(3);

        verify(userServiceRestClient).findUserById(userId);
        verify(itemRepository).findByIdIn(List.of(1L, 2L, 3L));
        verify(orderMapper).orderToOrderResponse(any(), any());
    }

    @Test
    @DisplayName("Should return order by id and userId successfully")
    void findByIdAndUserId_ShouldReturnOrder_WhenOrderExists() {
        Long orderId = 1L;
        Long userId = 1L;
        UserResponse userResponse = createUserResponse(userId);
        Order order = createOrder(orderId, userId, OrderStatus.NEW);
        OrderResponse expectedResponse = createOrderResponse(orderId, userId, OrderStatus.NEW, userResponse);

        when(userServiceRestClient.findUserById(userId)).thenReturn(userResponse);
        when(orderRepository.findByIdAndUserId(orderId, userId)).thenReturn(Optional.of(order));
        when(orderMapper.orderToOrderResponse(order, userResponse)).thenReturn(expectedResponse);

        OrderResponse actualResponse = orderService.findByIdAndUserId(orderId, userId);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getId()).isEqualTo(expectedResponse.getId());
        assertThat(actualResponse.getUserId()).isEqualTo(expectedResponse.getUserId());
        assertThat(actualResponse.getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(actualResponse.getUser()).isNotNull();
        assertThat(actualResponse.getUser().getId()).isEqualTo(userId);

        verify(userServiceRestClient).findUserById(userId);
        verify(orderRepository).findByIdAndUserId(orderId, userId);
        verify(orderMapper).orderToOrderResponse(order, userResponse);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when order not found by id and userId")
    void findByIdAndUserId_ShouldThrowException_WhenOrderNotFound() {
        Long orderId = 999L;
        Long userId = 1L;
        UserResponse userResponse = createUserResponse(userId);

        when(userServiceRestClient.findUserById(userId)).thenReturn(userResponse);
        when(orderRepository.findByIdAndUserId(orderId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findByIdAndUserId(orderId, userId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userServiceRestClient).findUserById(userId);
        verify(orderRepository).findByIdAndUserId(orderId, userId);
        verify(orderMapper, never()).orderToOrderResponse(any(), any());
    }

    @Test
    @DisplayName("Should return filtered orders successfully")
    void findByFilter_ShouldReturnOrders() {
        FilterRequest filterRequest = FilterRequest.builder()
                .ids(List.of(1L, 2L))
                .statuses(List.of(OrderStatus.NEW))
                .build();
        PageableRequest pageableRequest = new PageableRequest(10, 0);
        
        Order order1 = createOrder(1L, 1L, OrderStatus.NEW);
        Order order2 = createOrder(2L, 2L, OrderStatus.NEW);
        List<Order> orders = List.of(order1, order2);
        
        UserResponse user1 = createUserResponse(1L);
        UserResponse user2 = createUserResponse(2L);
        List<UserResponse> users = List.of(user1, user2);
        
        OrderResponse orderResponse1 = createOrderResponse(1L, 1L, OrderStatus.NEW, user1);
        OrderResponse orderResponse2 = createOrderResponse(2L, 2L, OrderStatus.NEW, user2);
        List<OrderResponse> expectedResponses = List.of(orderResponse1, orderResponse2);

        when(orderRepository.findAll(isA(Specification.class), isA(PageRequest.class))).thenReturn(orders);
        when(userServiceRestClient.findUsersByIds(anySet())).thenReturn(users);
        when(orderMapper.ordersToOrdersResponse(anyList(), anyMap())).thenReturn(expectedResponses);

        List<OrderResponse> actualResponses = orderService.findByFilter(filterRequest, pageableRequest);

        assertThat(actualResponses).isNotNull()
                .hasSize(2);
        assertThat(actualResponses.get(0).getId()).isEqualTo(1L);
        assertThat(actualResponses.get(1).getId()).isEqualTo(2L);

        verify(orderRepository).findAll(isA(Specification.class), isA(PageRequest.class));
        verify(userServiceRestClient).findUsersByIds(Set.of(1L, 2L));
        verify(orderMapper).ordersToOrdersResponse(anyList(), anyMap());
    }

    @Test
    @DisplayName("Should return empty list when no orders match filter")
    void findByFilter_ShouldReturnEmptyList_WhenNoOrdersFound() {
        FilterRequest filterRequest = FilterRequest.builder()
                .ids(List.of(999L))
                .build();
        PageableRequest pageableRequest = new PageableRequest(10, 0);

        when(orderRepository.findAll(isA(Specification.class), isA(PageRequest.class))).thenReturn(List.of());
        when(userServiceRestClient.findUsersByIds(anySet())).thenReturn(List.of());
        when(orderMapper.ordersToOrdersResponse(anyList(), anyMap())).thenReturn(List.of());

        List<OrderResponse> actualResponses = orderService.findByFilter(filterRequest, pageableRequest);

        assertThat(actualResponses).isNotNull()
                .isEmpty();

        verify(orderRepository).findAll(isA(Specification.class), isA(PageRequest.class));
        verify(userServiceRestClient).findUsersByIds(anySet());
        verify(orderMapper).ordersToOrdersResponse(anyList(), anyMap());
    }

    @Test
    @DisplayName("Should update order successfully")
    void updateById_ShouldUpdateOrder() {
        Long orderId = 1L;
        Long userId = 1L;
        OrderRequest updateRequest = createOrderRequest(OrderStatus.PREPARED);
        
        List<Item> items = createItems();
        Order existingOrder = createOrderWithItems(orderId, userId, OrderStatus.NEW, items);
        UserResponse userResponse = createUserResponse(userId);
        OrderResponse expectedResponse = createOrderResponse(orderId, userId, OrderStatus.PREPARED, userResponse);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(itemRepository.findByIdIn(anyList())).thenReturn(items);
        when(userServiceRestClient.findUserById(userId)).thenReturn(userResponse);
        when(orderMapper.orderToOrderResponse(any(), any())).thenReturn(expectedResponse);

        OrderResponse actualResponse = orderService.updateById(orderId, updateRequest);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getId()).isEqualTo(orderId);
        assertThat(actualResponse.getStatus()).isEqualTo(OrderStatus.PREPARED);

        verify(orderRepository).findById(orderId);
        verify(itemRepository).findByIdIn(anyList());
        verify(userServiceRestClient).findUserById(userId);
        verify(orderMapper).orderToOrderResponse(any(), any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent order")
    void updateById_ShouldThrowException_WhenOrderNotFound() {
        Long orderId = 999L;
        OrderRequest updateRequest = createOrderRequest(OrderStatus.PREPARED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateById(orderId, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(orderRepository).findById(orderId);
        verify(itemRepository, never()).findByIdIn(anyList());
        verify(userServiceRestClient, never()).findUserById(any());
        verify(orderMapper, never()).orderToOrderResponse(any(), any());
    }

    @Test
    @DisplayName("Should delete order successfully")
    void deleteById_ShouldDeleteOrder() {
        Long orderId = 1L;

        when(orderRepository.existsById(orderId)).thenReturn(true);
        doNothing().when(orderRepository).deleteById(orderId);

        orderService.deleteById(orderId);

        verify(orderRepository).existsById(orderId);
        verify(orderRepository).deleteById(orderId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent order")
    void deleteById_ShouldThrowException_WhenOrderNotFound() {
        Long orderId = 999L;

        when(orderRepository.existsById(orderId)).thenReturn(false);

        assertThatThrownBy(() -> orderService.deleteById(orderId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(orderRepository).existsById(orderId);
        verify(orderRepository, never()).deleteById(orderId);
    }

}

package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.exception.ResourceNotFoundException;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.model.dto.CreateOrderEvent;
import com.innowise.orderservice.model.dto.FilterRequest;
import com.innowise.orderservice.model.dto.OrderItemRequest;
import com.innowise.orderservice.model.dto.OrderRequest;
import com.innowise.orderservice.model.dto.OrderResponse;
import com.innowise.orderservice.model.dto.PageableRequest;
import com.innowise.orderservice.model.dto.UserResponse;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderItem;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.service.KafkaService;
import com.innowise.orderservice.service.OrderService;
import com.innowise.orderservice.service.UserServiceRestClient;
import com.innowise.orderservice.specification.OrderSpecification;
import com.innowise.orderservice.util.ExceptionMessageGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final UserServiceRestClient userServiceRestClient;
    private final KafkaService kafkaService;

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;

    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponse save(Long userId, OrderRequest orderRequest) {
        UserResponse userResponse = userServiceRestClient.findUserById(userId);

        Order order = Order.builder()
                .userId(userId)
                .status(orderRequest.getStatus())
                .build();
        updateOrderItems(order, orderRequest);

        orderRepository.save(order);

        CreateOrderEvent createOrderEvent = CreateOrderEvent.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .paymentAmount(order.getOrderItems().stream()
                        .map(orderItem -> orderItem.getItem().getPrice()
                                .multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .build();

        kafkaService.sendCreateOrderEvent(createOrderEvent);

        return orderMapper.orderToOrderResponse(order, userResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse findByIdAndUserId(Long id, Long userId) {
        UserResponse userResponse = userServiceRestClient.findUserById(userId);

        Order order = orderRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException(ExceptionMessageGenerator.orderNotFound(id)));

        return orderMapper.orderToOrderResponse(order, userResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findByFilter(FilterRequest filterRequest, PageableRequest pageableRequest) {
        PageRequest pageRequest = PageRequest.of(pageableRequest.getPage(), pageableRequest.getSize());
        Specification<Order> specification = Specification.unrestricted();

        if (filterRequest.getIds() != null && !filterRequest.getIds().isEmpty()) {
            specification = specification.and(OrderSpecification.idIn(filterRequest.getIds()));
        }
        if (filterRequest.getStatuses() != null && !filterRequest.getStatuses().isEmpty()) {
            specification = specification.and(OrderSpecification.statusIn(filterRequest.getStatuses()));
        }

        List<Order> orders = orderRepository.findAll(specification, pageRequest);

        List<UserResponse> usersResponse = userServiceRestClient.findUsersByIds(orders.stream()
                .map(Order::getUserId)
                .collect(Collectors.toSet())
        );

        Map<Long, UserResponse> userMap = usersResponse.stream()
                .collect(Collectors.toMap(UserResponse::getId, Function.identity()));

        return orderMapper.ordersToOrdersResponse(orders, userMap);
    }

    @Override
    @Transactional
    public OrderResponse updateById(Long id, OrderRequest orderRequest) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ExceptionMessageGenerator.orderNotFound(id)));

        order.setStatus(orderRequest.getStatus());
        updateOrderItems(order, orderRequest);

        UserResponse userResponse = userServiceRestClient.findUserById(order.getUserId());

        return orderMapper.orderToOrderResponse(order, userResponse);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException(ExceptionMessageGenerator.orderNotFound(id));
        }

        orderRepository.deleteById(id);
    }

    private void updateOrderItems(Order order, OrderRequest orderRequest) {
        Map<Long, Item> itemMap = validateAndGetItems(orderRequest);

        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            List<OrderItem> newOrderItems = createOrderItems(order, orderRequest, itemMap);
            order.setOrderItems(newOrderItems);
        } else {
            smartUpdateOrderItems(order, orderRequest, itemMap);
        }
    }

    private Map<Long, Item> validateAndGetItems(OrderRequest orderRequest) {
        List<Long> itemIds = orderRequest.getOrderItems().stream()
                .map(OrderItemRequest::getItemId)
                .toList();

        List<Item> items = itemRepository.findByIdIn(itemIds);

        if (itemIds.size() != items.size()) {
            throw new ResourceNotFoundException(ExceptionMessageGenerator.notAllItemsFound());
        }

        return items.stream()
                .collect(Collectors.toMap(Item::getId, Function.identity()));
    }

    private List<OrderItem> createOrderItems(Order order, OrderRequest orderRequest, Map<Long, Item> itemMap) {
        return orderRequest.getOrderItems().stream()
                .map(orderItemRequest -> OrderItem.builder()
                        .order(order)
                        .item(itemMap.get(orderItemRequest.getItemId()))
                        .quantity(orderItemRequest.getQuantity())
                        .build())
                .toList();
    }

    private void smartUpdateOrderItems(Order order, OrderRequest orderRequest, Map<Long, Item> itemMap) {
        Map<Long, OrderItem> existingItemsMap = order.getOrderItems().stream()
                .collect(Collectors.toMap(oi -> oi.getItem().getId(), Function.identity()));

        Map<Long, OrderItemRequest> newItemsMap = orderRequest.getOrderItems().stream()
                .collect(Collectors.toMap(OrderItemRequest::getItemId, Function.identity()));

        order.getOrderItems().removeIf(existingItem -> {
            Long itemId = existingItem.getItem().getId();
            OrderItemRequest newItem = newItemsMap.get(itemId);

            if (newItem == null) {
                return true;
            } else {
                if (!existingItem.getQuantity().equals(newItem.getQuantity())) {
                    existingItem.setQuantity(newItem.getQuantity());
                }
                return false;
            }
        });

        newItemsMap.forEach((itemId, orderItemRequest) -> {
            if (!existingItemsMap.containsKey(itemId)) {
                OrderItem newOrderItem = OrderItem.builder()
                        .order(order)
                        .item(itemMap.get(itemId))
                        .quantity(orderItemRequest.getQuantity())
                        .build();
                order.getOrderItems().add(newOrderItem);
            }
        });
    }

}

package com.innowise.orderservice.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.orderservice.controller.OrderController;
import com.innowise.orderservice.exception.ResourceNotFoundException;
import com.innowise.orderservice.model.OrderStatus;
import com.innowise.orderservice.model.dto.FilterRequest;
import com.innowise.orderservice.model.dto.OrderRequest;
import com.innowise.orderservice.model.dto.OrderResponse;
import com.innowise.orderservice.model.dto.PageableRequest;
import com.innowise.orderservice.model.dto.UserResponse;
import com.innowise.orderservice.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.innowise.orderservice.util.OrderUtil.createOrderRequest;
import static com.innowise.orderservice.util.OrderUtil.createOrderResponse;
import static com.innowise.orderservice.util.OrderUtil.createUserResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = OrderController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should create order successfully")
    void save_ShouldCreateOrder_WhenValidRequest() throws Exception {
        OrderRequest orderRequest = createOrderRequest(OrderStatus.NEW);
        UserResponse userResponse = createUserResponse(1L);
        OrderResponse orderResponse = createOrderResponse(1L, 1L, OrderStatus.NEW, userResponse);

        when(orderService.save(any(), any(OrderRequest.class))).thenReturn(orderResponse);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.user.id").value(1L));
    }

    @Test
    @DisplayName("Should return 400 when creating order with invalid data")
    void save_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        OrderRequest orderRequest = new OrderRequest();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return order by id and userId successfully")
    void findByIdAndUserId_ShouldReturnOrder_WhenOrderExists() throws Exception {
        Long orderId = 1L;
        Long userId = 1L;
        OrderResponse orderResponse = createOrderResponse(orderId, userId, OrderStatus.NEW, createUserResponse(userId));

        when(orderService.findByIdAndUserId(orderId, userId)).thenReturn(orderResponse);

        mockMvc.perform(get("/api/v1/orders/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.user.id").value(userId));
    }

    @Test
    @DisplayName("Should return 404 when order not found by id and userId")
    void findByIdAndUserId_ShouldReturnNotFound_WhenOrderNotFound() throws Exception {
        Long orderId = 1L;
        Long userId = 1L;

        when(orderService.findByIdAndUserId(orderId, userId))
                .thenThrow(new ResourceNotFoundException("Order not found"));

        mockMvc.perform(get("/api/v1/orders/{id}", orderId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return filtered orders successfully")
    void findByFilter_ShouldReturnOrders_WhenFilterProvided() throws Exception {
        UserResponse user1 = createUserResponse(1L);
        UserResponse user2 = createUserResponse(2L);
        List<OrderResponse> ordersResponse = List.of(
                createOrderResponse(1L, 1L, OrderStatus.NEW, user1),
                createOrderResponse(2L, 2L, OrderStatus.NEW, user2)
        );

        when(orderService.findByFilter(any(FilterRequest.class), any(PageableRequest.class)))
                .thenReturn(ordersResponse);

        mockMvc.perform(get("/api/v1/orders")
                        .param("size", "10")
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    @DisplayName("Should update order successfully")
    void updateById_ShouldUpdateOrder_WhenValidRequest() throws Exception {
        Long orderId = 1L;
        OrderRequest orderRequest = createOrderRequest(OrderStatus.PREPARED);
        UserResponse userResponse = createUserResponse(1L);
        OrderResponse orderResponse = createOrderResponse(orderId, 1L, OrderStatus.PREPARED, userResponse);

        when(orderService.updateById(eq(orderId), any(OrderRequest.class))).thenReturn(orderResponse);

        mockMvc.perform(put("/api/v1/orders/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value("PREPARED"));
    }

    @Test
    @DisplayName("Should return 400 when updating order with invalid data")
    void updateById_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        Long orderId = 1L;
        OrderRequest orderRequest = new OrderRequest();

        mockMvc.perform(put("/api/v1/orders/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent order")
    void updateById_ShouldReturnNotFound_WhenOrderNotFound() throws Exception {
        Long orderId = 999L;
        OrderRequest orderRequest = createOrderRequest(OrderStatus.PREPARED);

        when(orderService.updateById(eq(orderId), any(OrderRequest.class)))
                .thenThrow(new ResourceNotFoundException("Order not found"));

        mockMvc.perform(put("/api/v1/orders/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should delete order successfully")
    void deleteById_ShouldDeleteOrder_WhenOrderExists() throws Exception {
        Long orderId = 1L;

        doNothing().when(orderService).deleteById(orderId);

        mockMvc.perform(delete("/api/v1/orders/{id}", orderId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent order")
    void deleteById_ShouldReturnNotFound_WhenOrderNotFound() throws Exception {
        Long orderId = 999L;

        doThrow(new ResourceNotFoundException("Order not found"))
                .when(orderService).deleteById(orderId);

        mockMvc.perform(delete("/api/v1/orders/{id}", orderId))
                .andExpect(status().isNotFound());
    }

}

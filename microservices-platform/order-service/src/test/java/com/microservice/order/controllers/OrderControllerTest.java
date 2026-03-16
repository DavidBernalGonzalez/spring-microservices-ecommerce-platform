package com.microservice.order.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.order.dto.request.OrderItemRequestDto;
import com.microservice.order.dto.request.OrderRequestDto;
import com.microservice.order.dto.response.OrderItemResponseDto;
import com.microservice.order.dto.response.OrderResponseDto;
import com.microservice.order.entities.OrderStatus;
import com.microservice.order.services.OrderCreationResult;
import com.microservice.order.services.OrderService;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setCustomArgumentResolvers(
                        new org.springframework.data.web.PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Nested
    @DisplayName("GET /api/v1/orders")
    class GetAll {

        @Test
        @DisplayName("returns 200 with paginated orders")
        void returns200WithPaginatedOrders() throws Exception {
            OrderResponseDto order = OrderResponseDto.builder()
                    .id(1L)
                    .orderNumber("ORD-000001")
                    .total(BigDecimal.valueOf(100))
                    .status(OrderStatus.PENDING)
                    .orderItems(List.of())
                    .build();

            when(orderService.getAll(any()))
                    .thenReturn(new PageImpl<>(List.of(order), PageRequest.of(0, 20), 1));

            mockMvc.perform(get("/api/v1/orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].orderNumber").value("ORD-000001"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/orders/{id}")
    class GetById {

        @Test
        @DisplayName("returns 200 with order when found")
        void returns200WithOrderWhenFound() throws Exception {
            OrderResponseDto order = OrderResponseDto.builder()
                    .id(1L)
                    .orderNumber("ORD-000001")
                    .total(BigDecimal.valueOf(100))
                    .status(OrderStatus.PENDING)
                    .orderItems(List.of())
                    .build();

            when(orderService.getById(1L)).thenReturn(order);

            mockMvc.perform(get("/api/v1/orders/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.orderNumber").value("ORD-000001"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/orders")
    class Create {

        @Test
        @DisplayName("returns 201 when order is created")
        void returns201WhenOrderIsCreated() throws Exception {
            OrderRequestDto request = OrderRequestDto.builder()
                    .idempotencyKey("test-key-001")
                    .orderItems(List.of(
                            OrderItemRequestDto.builder()
                                    .productId(1L)
                                    .quantity(2)
                                    .discount(BigDecimal.ZERO)
                                    .build()))
                    .build();

            OrderResponseDto response = OrderResponseDto.builder()
                    .id(1L)
                    .orderNumber("ORD-000001")
                    .total(BigDecimal.valueOf(241.98))
                    .status(OrderStatus.PENDING)
                    .orderItems(List.of(
                            OrderItemResponseDto.builder()
                                    .productId(1L)
                                    .productName("Test")
                                    .quantity(2)
                                    .unitPrice(BigDecimal.valueOf(99.99))
                                    .build()))
                    .build();

            com.microservice.order.entities.Order order = com.microservice.order.mapper.OrderMapper.toEntity(request);
            order.setId(1L);
            order.setOrderNumber("ORD-000001");

            when(orderService.create(any()))
                    .thenReturn(new OrderCreationResult(order, true));

            when(orderService.toResponseDto(any()))
                    .thenReturn(response);

            mockMvc.perform(post("/api/v1/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.orderNumber").value("ORD-000001"));
        }

        @Test
        @DisplayName("returns 400 when orderItems is empty")
        void returns400WhenOrderItemsIsEmpty() throws Exception {
            OrderRequestDto request = OrderRequestDto.builder()
                    .idempotencyKey("test-key")
                    .orderItems(List.of())
                    .build();

            mockMvc.perform(post("/api/v1/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}

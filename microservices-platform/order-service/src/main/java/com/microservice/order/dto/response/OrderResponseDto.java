package com.microservice.order.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.microservice.order.entities.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponseDto {

    private Long id;
    private String orderNumber;
    private String idempotencyKey;
    private LocalDateTime createdAt;

    private BigDecimal pvpSubtotal;
    private BigDecimal discountTotal;
    private BigDecimal taxTotal;
    private BigDecimal total;

    private OrderStatus status;

    private List<OrderItemResponseDto> orderItems;

    /** Present and true when this order was returned due to idempotent replay (same idempotencyKey). */
    private Boolean idempotentReplay;
}
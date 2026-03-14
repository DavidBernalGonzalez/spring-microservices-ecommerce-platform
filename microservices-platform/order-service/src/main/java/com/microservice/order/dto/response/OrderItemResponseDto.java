package com.microservice.order.dto.response;

import java.math.BigDecimal;

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
public class OrderItemResponseDto {

    private Long id;
    private Long productId;
    private String productName;

    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal finalPrice;

    private Integer quantity;

    private BigDecimal lineSubtotal;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal lineTotal;
}
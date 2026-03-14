package com.microservice.order.mapper;

import java.util.List;

import com.microservice.order.dto.request.OrderItemRequestDto;
import com.microservice.order.dto.request.OrderRequestDto;
import com.microservice.order.dto.response.OrderItemResponseDto;
import com.microservice.order.dto.response.OrderResponseDto;
import com.microservice.order.entities.Order;
import com.microservice.order.entities.OrderItem;

public class OrderMapper {

    private OrderMapper() {
    }

    public static Order toEntity(OrderRequestDto dto) {
        if (dto == null) {
            return null;
        }

        Order order = Order.builder()
                .idempotencyKey(dto.getIdempotencyKey())
                .build();

        if (dto.getOrderItems() != null) {
            dto.getOrderItems().forEach(itemDto -> {
                OrderItem item = fromItemRequest(itemDto);
                order.addItem(item);
            });
        }

        return order;
    }

    private static OrderItem fromItemRequest(OrderItemRequestDto dto) {
        if (dto == null) {
            return null;
        }

        return OrderItem.builder()
                .productId(dto.getProductId())
                .quantity(dto.getQuantity())
                .discount(dto.getDiscount())
                .build();
    }

    public static OrderResponseDto toDto(Order order) {
        if (order == null) {
            return null;
        }

        List<OrderItemResponseDto> items = order.getOrderItems()
                .stream()
                .map(OrderMapper::toItemDto)
                .toList();

        return OrderResponseDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .idempotencyKey(order.getIdempotencyKey())
                .createdAt(order.getCreatedAt())
                .pvpSubtotal(order.getPvpSubtotal())
                .discountTotal(order.getDiscountTotal())
                .taxTotal(order.getTaxTotal())
                .total(order.getTotal())
                .status(order.getStatus())
                .orderItems(items)
                .build();
    }

    private static OrderItemResponseDto toItemDto(OrderItem item) {
        if (item == null) {
            return null;
        }

        return OrderItemResponseDto.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .unitPrice(item.getUnitPrice())
                .discount(item.getDiscount())
                .finalPrice(item.getFinalPrice())
                .quantity(item.getQuantity())
                .lineSubtotal(item.getLineSubtotal())
                .taxRate(item.getTaxRate())
                .taxAmount(item.getTaxAmount())
                .lineTotal(item.getLineTotal())
                .build();
    }
}
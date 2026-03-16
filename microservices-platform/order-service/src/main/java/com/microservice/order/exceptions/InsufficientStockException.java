package com.microservice.order.exceptions;

import lombok.Getter;

@Getter
public class InsufficientStockException extends RuntimeException {

    private final Long productId;
    private final Integer requestedQuantity;
    private final String detail;

    public InsufficientStockException(Long productId, Integer requestedQuantity, String detail) {
        super("Insufficient stock for product " + productId + ": " + detail);
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.detail = detail;
    }
}

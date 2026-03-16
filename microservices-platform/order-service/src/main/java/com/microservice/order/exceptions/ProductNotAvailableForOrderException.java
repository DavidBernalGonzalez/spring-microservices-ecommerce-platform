package com.microservice.order.exceptions;

import lombok.Getter;

@Getter
public class ProductNotAvailableForOrderException extends RuntimeException {

    private final Long productId;
    private final String currentStatus;

    public ProductNotAvailableForOrderException(Long productId, String currentStatus) {
        super("Product " + productId + " is not available for purchase (status: " + currentStatus + ")");
        this.productId = productId;
        this.currentStatus = currentStatus;
    }
}

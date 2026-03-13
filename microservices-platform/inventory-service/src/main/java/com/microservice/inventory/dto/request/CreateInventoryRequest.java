package com.microservice.inventory.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateInventoryRequest {

    @NotNull(message = "Product id is required")
    private Long productId;

    @NotNull(message = "Initial stock is required")
    @Min(value = 0, message = "Initial stock cannot be negative")
    private Integer initialStock;
}
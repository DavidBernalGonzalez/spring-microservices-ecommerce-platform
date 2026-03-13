package com.microservice.inventory.dto.request;

import com.microservice.inventory.enums.InventoryReferenceType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockAdjustmentRequest {

    @NotNull(message = "referenceType is required")
    private InventoryReferenceType referenceType;

    private String referenceNumber;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be greater than 0")
    private Integer quantity;

    private String reason;
}
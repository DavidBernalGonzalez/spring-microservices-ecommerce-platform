package com.microservice.inventory.dto.request;

import com.microservice.inventory.enums.InventoryReferenceType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "referenceNumber is required")
    private String referenceNumber;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "quantity must be greater than 0")
    private Integer quantity;

    @NotBlank(message = "reason is required")
    private String reason;
}
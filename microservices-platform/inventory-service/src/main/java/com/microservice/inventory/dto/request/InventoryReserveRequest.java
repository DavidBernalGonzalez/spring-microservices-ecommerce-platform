package com.microservice.inventory.dto.request;

import com.microservice.inventory.enums.InventoryReferenceType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReserveRequest {

    @NotNull(message = "referenceType is required")
    private InventoryReferenceType referenceType;

    @NotBlank(message = "referenceNumber is required")
    private String referenceNumber;

    @NotNull(message = "quantity is required")
    @Min(value = 1, message = "quantity must be greater than 0")
    private Integer quantity;

    private String reason;
}
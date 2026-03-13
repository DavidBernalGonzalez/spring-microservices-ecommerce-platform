package com.microservice.inventory.dto.response;

import java.time.LocalDateTime;

import com.microservice.inventory.enums.InventoryMovementType;
import com.microservice.inventory.enums.InventoryReferenceType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMovementResponse {

    private Long id;
    private Long productId;

    private InventoryReferenceType referenceType;
    private String referenceNumber;

    private Integer quantity;
    private InventoryMovementType movementType;
    private String reason;
    private LocalDateTime createdAt;
}
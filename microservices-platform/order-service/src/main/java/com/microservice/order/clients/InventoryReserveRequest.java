package com.microservice.order.clients;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReserveRequest {

    private String referenceType;  // "ORDER"
    private String referenceNumber;
    private Integer quantity;
    private String reason;
}

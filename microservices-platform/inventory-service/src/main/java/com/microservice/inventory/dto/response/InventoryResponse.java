package com.microservice.inventory.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponse {

    private Long productId;
    private Integer availableStock;
    private Integer reservedStock;
    private Integer totalStock;
}
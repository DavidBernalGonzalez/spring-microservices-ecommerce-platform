package com.microservice.order.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "inventory-service", url = "${services.inventory.base-url}")
public interface InventoryClient {

    @PostMapping("/api/v1/inventory/{productId}/reserve")
    Object reserveStock(
            @PathVariable("productId") Long productId,
            @RequestBody InventoryReserveRequest request);

    @PostMapping("/api/v1/inventory/{productId}/release")
    Object releaseStock(
            @PathVariable("productId") Long productId,
            @RequestBody InventoryReserveRequest request);
}

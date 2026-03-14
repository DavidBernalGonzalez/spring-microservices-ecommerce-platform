package com.microservice.inventory.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.microservice.inventory.dto.request.CreateInventoryRequest;
import com.microservice.inventory.dto.request.StockAdjustmentRequest;
import com.microservice.inventory.dto.response.InventoryMovementsResponse;
import com.microservice.inventory.dto.response.InventoryResponse;
import com.microservice.inventory.services.InventoryService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private static final String SERVICE = "inventory-service";

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ResponseEntity<List<InventoryResponse>> getAll() {
        log.info("[{}] GET /api/v1/inventory - fetching all inventory records", SERVICE);
        return ResponseEntity.ok(inventoryService.findAll());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponse> getByProductId(@PathVariable Long productId) {
        log.info("[{}] GET /api/v1/inventory/{} - fetching inventory", SERVICE, productId);
        return ResponseEntity.ok(inventoryService.findByProductId(productId));
    }

    @GetMapping("/{productId}/movements")
    public ResponseEntity<InventoryMovementsResponse> getMovementsByProductId(@PathVariable Long productId) {
        log.info("[{}] GET /api/v1/inventory/{}/movements - fetching inventory movements", SERVICE, productId);
        return ResponseEntity.ok(inventoryService.getMovementsByProductId(productId));
    }

    @PostMapping
    public ResponseEntity<InventoryResponse> create(@Valid @RequestBody CreateInventoryRequest request) {
        log.info("[{}] POST /api/v1/inventory - creating inventory productId={} initialStock={}",
                SERVICE,
                request.getProductId(),
                request.getInitialStock());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.create(request.getProductId(), request.getInitialStock()));
    }

    @PostMapping("/{productId}/add")
    public ResponseEntity<InventoryResponse> addStock(
            @PathVariable Long productId,
            @Valid @RequestBody StockAdjustmentRequest request) {

        log.info("[{}] POST /api/v1/inventory/{}/add - adding stock referenceType={} referenceNumber={} quantity={} reason={}",
                SERVICE,
                productId,
                request.getReferenceType(),
                request.getReferenceNumber(),
                request.getQuantity(),
                request.getReason());

        return ResponseEntity.ok(
                inventoryService.addStock(
                        productId,
                        request.getReferenceType(),
                        request.getReferenceNumber(),
                        request.getQuantity(),
                        request.getReason()));
    }

    @PostMapping("/{productId}/reserve")
    public ResponseEntity<InventoryResponse> reserveStock(
            @PathVariable Long productId,
            @Valid @RequestBody StockAdjustmentRequest request) {

        log.info("[{}] POST /api/v1/inventory/{}/reserve - reserving stock referenceType={} referenceNumber={} quantity={} reason={}",
                SERVICE,
                productId,
                request.getReferenceType(),
                request.getReferenceNumber(),
                request.getQuantity(),
                request.getReason());

        return ResponseEntity.ok(
                inventoryService.reserveStock(
                        productId,
                        request.getReferenceType(),
                        request.getReferenceNumber(),
                        request.getQuantity(),
                        request.getReason()));
    }

    @PostMapping("/{productId}/release")
    public ResponseEntity<InventoryResponse> releaseStock(
            @PathVariable Long productId,
            @Valid @RequestBody StockAdjustmentRequest request) {

        log.info("[{}] POST /api/v1/inventory/{}/release - releasing stock referenceType={} referenceNumber={} quantity={} reason={}",
                SERVICE,
                productId,
                request.getReferenceType(),
                request.getReferenceNumber(),
                request.getQuantity(),
                request.getReason());

        return ResponseEntity.ok(
                inventoryService.releaseStock(
                        productId,
                        request.getReferenceType(),
                        request.getReferenceNumber(),
                        request.getQuantity(),
                        request.getReason()));
    }

    @PostMapping("/{productId}/confirm-output")
    public ResponseEntity<InventoryResponse> confirmOutput(
            @PathVariable Long productId,
            @Valid @RequestBody StockAdjustmentRequest request) {

        log.info("[{}] POST /api/v1/inventory/{}/confirm-output - confirming stock output referenceType={} referenceNumber={} quantity={} reason={}",
                SERVICE,
                productId,
                request.getReferenceType(),
                request.getReferenceNumber(),
                request.getQuantity(),
                request.getReason());

        return ResponseEntity.ok(
                inventoryService.confirmOutput(
                        productId,
                        request.getReferenceType(),
                        request.getReferenceNumber(),
                        request.getQuantity(),
                        request.getReason()));
    }
}
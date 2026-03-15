package com.microservice.inventory.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.microservice.inventory.clients.ProductClient;
import com.microservice.inventory.dto.response.InventoryMovementResponse;
import com.microservice.inventory.dto.response.InventoryMovementsResponse;
import com.microservice.inventory.dto.response.InventoryResponse;
import com.microservice.inventory.entities.Inventory;
import com.microservice.inventory.entities.InventoryMovement;
import com.microservice.inventory.enums.InventoryMovementType;
import com.microservice.inventory.enums.InventoryReferenceType;
import com.microservice.inventory.repositories.InventoryMovementRepository;
import com.microservice.inventory.repositories.InventoryRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class InventoryService {

    private static final String SERVICE = "inventory-service";

    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository movementRepository;
    private final ProductClient productClient;

    public InventoryService(
            InventoryRepository inventoryRepository,
            InventoryMovementRepository movementRepository,
            ProductClient productClient) {
        this.inventoryRepository = inventoryRepository;
        this.movementRepository = movementRepository;
        this.productClient = productClient;
    }

    @Transactional(readOnly = true)
    public Page<InventoryResponse> findAll(Pageable pageable) {
        log.info("[{}] Fetching inventories page={} size={}", SERVICE, pageable.getPageNumber(), pageable.getPageSize());

        return inventoryRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public InventoryResponse findByProductId(Long productId) {
        log.info("[{}] Fetching inventory for productId={}", SERVICE, productId);

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Inventory not found for productId: " + productId));

        return toResponse(inventory);
    }

    @Transactional(readOnly = true)
    public InventoryMovementsResponse getMovementsByProductId(Long productId) {
        log.info("[{}] Fetching movements for productId={}", SERVICE, productId);

        if (!inventoryRepository.existsByProductId(productId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Inventory not found for productId: " + productId);
        }

        List<InventoryMovementResponse> movements = movementRepository
                .findByInventoryProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .map(movement -> InventoryMovementResponse.builder()
                        .id(movement.getId())
                        .referenceType(movement.getReferenceType())
                        .referenceNumber(movement.getReferenceNumber())
                        .quantity(movement.getQuantity())
                        .movementType(movement.getMovementType())
                        .reason(movement.getReason())
                        .createdAt(movement.getCreatedAt())
                        .build())
                .toList();

        return InventoryMovementsResponse.builder()
                .productId(productId)
                .movements(movements)
                .build();
    }

    @Transactional
    public InventoryResponse create(Long productId, Integer initialStock) {
        log.info("[{}] Creating inventory productId={} initialStock={}", SERVICE, productId, initialStock);

        validateQuantity(initialStock);

        productClient.validateProductExists(productId);

        if (inventoryRepository.existsByProductId(productId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Inventory already exists for productId: " + productId);
        }

        Inventory inventory = Inventory.builder()
                .productId(productId)
                .availableStock(initialStock)
                .reservedStock(0)
                .build();

        Inventory saved = inventoryRepository.save(inventory);

        movementRepository.save(InventoryMovement.builder()
                .inventory(saved)
                .referenceType(InventoryReferenceType.INITIAL_STOCK)
                .referenceNumber("INITIAL-STOCK")
                .quantity(initialStock)
                .movementType(InventoryMovementType.IN)
                .reason("Initial stock")
                .build());

        return toResponse(saved);
    }

    @Transactional
    public InventoryResponse addStock(
            Long productId,
            InventoryReferenceType referenceType,
            String referenceNumber,
            Integer quantity,
            String reason) {

        validateQuantity(quantity);
        validateReference(referenceType, referenceNumber);
        validateAddStockReferenceType(referenceType);

        Inventory inventory = getInventory(productId);

        inventory.setAvailableStock(inventory.getAvailableStock() + quantity);
        Inventory saved = inventoryRepository.save(inventory);

        movementRepository.save(InventoryMovement.builder()
                .inventory(saved)
                .referenceType(referenceType)
                .referenceNumber(referenceNumber.trim())
                .quantity(quantity)
                .movementType(InventoryMovementType.IN)
                .reason(reason)
                .build());

        log.info("[{}] Stock added productId={} referenceType={} referenceNumber={} quantity={} availableStock={}",
                SERVICE, productId, referenceType, referenceNumber, quantity, saved.getAvailableStock());

        return toResponse(saved);
    }

    @Transactional
    public InventoryResponse reserveStock(
            Long productId,
            InventoryReferenceType referenceType,
            String referenceNumber,
            Integer quantity,
            String reason) {

        validateQuantity(quantity);
        validateReference(referenceType, referenceNumber);
        validateOrderReferenceType(referenceType);

        Inventory inventory = getInventory(productId);

        if (inventory.getAvailableStock() < quantity) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Insufficient available stock for productId: " + productId);
        }

        inventory.setAvailableStock(inventory.getAvailableStock() - quantity);
        inventory.setReservedStock(inventory.getReservedStock() + quantity);

        Inventory saved = inventoryRepository.save(inventory);

        movementRepository.save(InventoryMovement.builder()
                .inventory(saved)
                .referenceType(referenceType)
                .referenceNumber(referenceNumber.trim())
                .quantity(quantity)
                .movementType(InventoryMovementType.RESERVE)
                .reason(reason)
                .build());

        log.info("[{}] Stock reserved productId={} referenceType={} referenceNumber={} quantity={} available={} reserved={}",
                SERVICE, productId, referenceType, referenceNumber, quantity,
                saved.getAvailableStock(), saved.getReservedStock());

        return toResponse(saved);
    }

    @Transactional
    public InventoryResponse releaseStock(
            Long productId,
            InventoryReferenceType referenceType,
            String referenceNumber,
            Integer quantity,
            String reason) {

        validateQuantity(quantity);
        validateReference(referenceType, referenceNumber);
        validateOrderReferenceType(referenceType);

        Inventory inventory = getInventory(productId);

        if (inventory.getReservedStock() < quantity) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Insufficient reserved stock for productId: " + productId);
        }

        inventory.setReservedStock(inventory.getReservedStock() - quantity);
        inventory.setAvailableStock(inventory.getAvailableStock() + quantity);

        Inventory saved = inventoryRepository.save(inventory);

        movementRepository.save(InventoryMovement.builder()
                .inventory(saved)
                .referenceType(referenceType)
                .referenceNumber(referenceNumber.trim())
                .quantity(quantity)
                .movementType(InventoryMovementType.RELEASE)
                .reason(reason)
                .build());

        log.info("[{}] Stock released productId={} referenceType={} referenceNumber={} quantity={} available={} reserved={}",
                SERVICE, productId, referenceType, referenceNumber, quantity,
                saved.getAvailableStock(), saved.getReservedStock());

        return toResponse(saved);
    }

    @Transactional
    public InventoryResponse confirmOutput(
            Long productId,
            InventoryReferenceType referenceType,
            String referenceNumber,
            Integer quantity,
            String reason) {

        validateQuantity(quantity);
        validateReference(referenceType, referenceNumber);
        validateOrderReferenceType(referenceType);

        Inventory inventory = getInventory(productId);

        if (inventory.getReservedStock() < quantity) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Insufficient reserved stock for productId: " + productId);
        }

        inventory.setReservedStock(inventory.getReservedStock() - quantity);

        Inventory saved = inventoryRepository.save(inventory);

        movementRepository.save(InventoryMovement.builder()
                .inventory(saved)
                .referenceType(referenceType)
                .referenceNumber(referenceNumber.trim())
                .quantity(quantity)
                .movementType(InventoryMovementType.OUT)
                .reason(reason)
                .build());

        log.info("[{}] Stock output confirmed productId={} referenceType={} referenceNumber={} quantity={} available={} reserved={}",
                SERVICE, productId, referenceType, referenceNumber, quantity,
                saved.getAvailableStock(), saved.getReservedStock());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    private Inventory getInventory(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Inventory not found for productId: " + productId));
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "quantity must be greater than 0");
        }
    }

    private void validateReference(InventoryReferenceType referenceType, String referenceNumber) {
        if (referenceType == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "referenceType is required");
        }

        if (referenceType == InventoryReferenceType.INITIAL_STOCK) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "INITIAL_STOCK cannot be used in external requests");
        }

        if (referenceNumber == null || referenceNumber.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "referenceNumber is required");
        }
    }

    private void validateOrderReferenceType(InventoryReferenceType referenceType) {
        if (referenceType != InventoryReferenceType.ORDER) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "referenceType must be ORDER for this operation");
        }
    }

    private void validateAddStockReferenceType(InventoryReferenceType referenceType) {
        if (referenceType != InventoryReferenceType.RESTOCK
                && referenceType != InventoryReferenceType.MANUAL_ADJUSTMENT) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "referenceType must be RESTOCK or MANUAL_ADJUSTMENT for add stock");
        }
    }

    private InventoryResponse toResponse(Inventory inventory) {
        return InventoryResponse.builder()
                .productId(inventory.getProductId())
                .availableStock(inventory.getAvailableStock())
                .reservedStock(inventory.getReservedStock())
                .totalStock(inventory.getAvailableStock() + inventory.getReservedStock())
                .build();
    }
}
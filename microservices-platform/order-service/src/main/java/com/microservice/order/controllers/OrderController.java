package com.microservice.order.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.microservice.order.dto.request.OrderRequestDto;
import com.microservice.order.dto.response.OrderResponseDto;
import com.microservice.order.services.OrderCreationResult;
import com.microservice.order.services.OrderService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponseDto>> getAll(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        log.info("GET /api/v1/orders - fetching orders page={} size={}", pageable.getPageNumber(), pageable.getPageSize());

        Page<OrderResponseDto> page = orderService.getAll(pageable);

        log.info("GET /api/v1/orders - returned page {} of {} ({} items)", page.getNumber(), page.getTotalPages(), page.getNumberOfElements());

        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getById(@PathVariable Long id) {
        log.info("GET /api/v1/orders/{} - fetching order", id);

        OrderResponseDto order = orderService.getById(id);

        log.info("GET /api/v1/orders/{} - order found orderNumber={}", id, order.getOrderNumber());

        return ResponseEntity.ok(order);
    }

    @PostMapping
    public ResponseEntity<OrderResponseDto> create(@Valid @RequestBody OrderRequestDto request) {

        log.info("POST /api/v1/orders - received request with idempotencyKey={} and {} items",
                request.getIdempotencyKey(),
                request.getOrderItems() != null ? request.getOrderItems().size() : 0);

        OrderCreationResult result = orderService.create(request);

        OrderResponseDto body = orderService.toResponseDto(result.getOrder());

        if (result.isCreated()) {

            log.info("Order created successfully. orderId={} orderNumber={}",
                    result.getOrder().getId(),
                    result.getOrder().getOrderNumber());

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(body);
        }

        log.info("Idempotent request detected. Returning existing order orderId={} orderNumber={}",
                result.getOrder().getId(),
                result.getOrder().getOrderNumber());

        body.setIdempotentReplay(true);

        return ResponseEntity
                .status(HttpStatus.OK)
                .header("X-Idempotent-Replay", "true")
                .body(body);
    }
}
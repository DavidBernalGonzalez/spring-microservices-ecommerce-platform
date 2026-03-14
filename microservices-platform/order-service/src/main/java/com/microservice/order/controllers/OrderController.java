package com.microservice.order.controllers;

import java.util.List;

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
@RequestMapping("api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getAll() {
        log.info("GET /api/orders - fetching all orders");

        var orders = orderService.getAll();

        log.info("GET /api/orders - returned {} orders", orders.size());

        return ResponseEntity.ok(orders);
    }

    @PostMapping
    public ResponseEntity<OrderResponseDto> create(@Valid @RequestBody OrderRequestDto request) {

        log.info("POST /api/orders - received request with idempotencyKey={} and {} items",
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

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(body);
    }
}
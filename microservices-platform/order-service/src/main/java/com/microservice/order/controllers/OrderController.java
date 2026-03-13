package com.microservice.order.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.microservice.order.entities.Order;
import com.microservice.order.services.OrderCreationResult;
import com.microservice.order.services.OrderService;

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
    public ResponseEntity<?> getAll() {
        log.info("GET /api/orders - fetching all orders");

        var orders = orderService.getAll();

        log.info("GET /api/orders - returned {} orders", orders.size());

        return ResponseEntity.ok(orders);
    }

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody Order order) {

        log.info("POST /api/orders - received request with idempotencyKey={} and {} items",
                order.getIdempotencyKey(),
                order.getOrderItems() != null ? order.getOrderItems().size() : 0);

        OrderCreationResult result = orderService.create(order);

        if (result.isCreated()) {

            log.info("Order created successfully. orderId={} orderNumber={}",
                    result.getOrder().getId(),
                    result.getOrder().getOrderNumber());

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(result.getOrder());
        }

        log.info("Idempotent request detected. Returning existing order orderId={} orderNumber={}",
                result.getOrder().getId(),
                result.getOrder().getOrderNumber());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(result.getOrder());
    }
}
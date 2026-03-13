package com.microservice.order.services;

import com.microservice.order.entities.Order;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderCreationResult {

    private final Order order;
    private final boolean created;
}
package com.microservice.order.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.microservice.order.clients.ProductClient;
import com.microservice.order.clients.ProductResponse;
import com.microservice.order.entities.Order;
import com.microservice.order.entities.OrderItem;
import com.microservice.order.repositories.OrderRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrderService {

    private static final String SERVICE = "order-service";

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final int MONEY_SCALE = 2;

    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    public OrderService(OrderRepository orderRepository, ProductClient productClient) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
    }

    public List<Order> getAll() {
        log.info("[{}] Fetching all orders", SERVICE);
        return orderRepository.findAll();
    }

    public OrderCreationResult create(Order order) {

        log.info("[{}] Creating order idempotencyKey={}", SERVICE, order.getIdempotencyKey());

        if (order.getIdempotencyKey() != null && !order.getIdempotencyKey().isBlank()) {
            Optional<Order> existingOrder = orderRepository.findByIdempotencyKey(order.getIdempotencyKey());

            if (existingOrder.isPresent()) {
                log.info("[{}] Idempotent request detected returning existing order orderId={}",
                        SERVICE,
                        existingOrder.get().getId());

                return new OrderCreationResult(existingOrder.get(), false);
            }
        }

        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            log.warn("[{}] Order creation failed no items provided", SERVICE);

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The order must contain at least one item"
            );
        }

        BigDecimal pvpSubtotal = ZERO;
        BigDecimal discountTotal = ZERO;
        BigDecimal taxTotal = ZERO;
        BigDecimal total = ZERO;

        for (OrderItem item : order.getOrderItems()) {

            log.info("[{}] Processing order item productId={} quantity={}",
                    SERVICE,
                    item.getProductId(),
                    item.getQuantity());

            ProductResponse product = productClient.getProductById(item.getProductId());

            if (product == null) {

                log.warn("[{}] Product not found productId={}", SERVICE, item.getProductId());

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Product not found: " + item.getProductId()
                );
            }

            if (product.getPrice() == null) {

                log.warn("[{}] Product price null productId={}", SERVICE, item.getProductId());

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Product price is null for product: " + item.getProductId()
                );
            }

            BigDecimal unitPrice = product.getPrice().setScale(MONEY_SCALE, RoundingMode.HALF_UP);

            BigDecimal taxRate = product.getTaxRate() != null
                    ? product.getTaxRate()
                    : ZERO;

            BigDecimal discount = item.getDiscount() != null
                    ? item.getDiscount().setScale(MONEY_SCALE, RoundingMode.HALF_UP)
                    : ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);

            BigDecimal finalPrice = unitPrice.subtract(discount);

            if (finalPrice.compareTo(ZERO) < 0) {

                log.warn("[{}] Final price negative productId={} finalPrice={}",
                        SERVICE,
                        item.getProductId(),
                        finalPrice);

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Final price cannot be negative for product: " + item.getProductId()
                );
            }

            BigDecimal quantity = BigDecimal.valueOf(item.getQuantity());

            BigDecimal grossLineSubtotal = unitPrice
                    .multiply(quantity)
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

            BigDecimal itemDiscountTotal = discount
                    .multiply(quantity)
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

            BigDecimal lineSubtotal = finalPrice
                    .multiply(quantity)
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

            BigDecimal taxAmount = lineSubtotal
                    .multiply(taxRate)
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

            BigDecimal lineTotal = lineSubtotal
                    .add(taxAmount)
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

            log.info("[{}] Calculated line productId={} subtotal={} tax={} total={}",
                    SERVICE,
                    item.getProductId(),
                    lineSubtotal,
                    taxAmount,
                    lineTotal);

            item.setProductName(product.getName());
            item.setUnitPrice(unitPrice);
            item.setDiscount(discount);
            item.setFinalPrice(finalPrice.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
            item.setLineSubtotal(lineSubtotal);
            item.setTaxRate(taxRate);
            item.setTaxAmount(taxAmount);
            item.setLineTotal(lineTotal);
            item.setOrder(order);

            pvpSubtotal = pvpSubtotal.add(grossLineSubtotal);
            discountTotal = discountTotal.add(itemDiscountTotal);
            taxTotal = taxTotal.add(taxAmount);
            total = total.add(lineTotal);
        }

        order.setPvpSubtotal(pvpSubtotal.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        order.setDiscountTotal(discountTotal.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        order.setTaxTotal(taxTotal.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        order.setTotal(total.setScale(MONEY_SCALE, RoundingMode.HALF_UP));

        log.info("[{}] Order totals subtotal={} discount={} tax={} total={}",
                SERVICE,
                pvpSubtotal,
                discountTotal,
                taxTotal,
                total);

        Order savedOrder = orderRepository.save(order);

        String orderNumber = "ORD-" + String.format("%06d", savedOrder.getId());
        savedOrder.setOrderNumber(orderNumber);

        Order finalOrder = orderRepository.save(savedOrder);

        log.info("[{}] Order created successfully orderId={} orderNumber={}",
                SERVICE,
                finalOrder.getId(),
                finalOrder.getOrderNumber());

        return new OrderCreationResult(finalOrder, true);
    }
}
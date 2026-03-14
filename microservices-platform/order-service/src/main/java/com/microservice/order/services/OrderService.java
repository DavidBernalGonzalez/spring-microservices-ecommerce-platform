package com.microservice.order.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.microservice.order.clients.ProductClient;
import com.microservice.order.clients.ProductResponse;
import com.microservice.order.dto.request.OrderRequestDto;
import com.microservice.order.dto.response.OrderResponseDto;
import com.microservice.order.entities.Order;
import com.microservice.order.entities.OrderItem;
import com.microservice.order.mapper.OrderMapper;
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

    public Page<OrderResponseDto> getAll(Pageable pageable) {
        log.info("[{}] Fetching orders page={} size={}", SERVICE, pageable.getPageNumber(), pageable.getPageSize());
        return orderRepository.findAllWithItems(pageable)
                .map(OrderMapper::toDto);
    }

    public OrderResponseDto getById(Long id) {
        log.info("[{}] Fetching order id={}", SERVICE, id);

        Order order = orderRepository.findWithItemsById(id)
                .orElseThrow(() -> {
                    log.warn("[{}] Order not found id={}", SERVICE, id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found with id: " + id);
                });

        log.info("[{}] Order found id={} orderNumber={}", SERVICE, order.getId(), order.getOrderNumber());

        return OrderMapper.toDto(order);
    }

    public OrderCreationResult create(OrderRequestDto request) {

        log.info("[{}] Creating order idempotencyKey={}", SERVICE, request.getIdempotencyKey());

        Order incomingOrder = OrderMapper.toEntity(request);

        if (incomingOrder.getIdempotencyKey() != null && !incomingOrder.getIdempotencyKey().isBlank()) {
            Optional<Order> existingOrder =
                    orderRepository.findWithItemsByIdempotencyKey(incomingOrder.getIdempotencyKey()); // 👈 también fetch join

            if (existingOrder.isPresent()) {
                log.info("[{}] Idempotent request detected returning existing order orderId={}",
                        SERVICE,
                        existingOrder.get().getId());

                return new OrderCreationResult(existingOrder.get(), false);
            }
        }

        if (incomingOrder.getOrderItems() == null || incomingOrder.getOrderItems().isEmpty()) {
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

        for (OrderItem item : incomingOrder.getOrderItems()) {

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
            item.setOrder(incomingOrder);

            pvpSubtotal = pvpSubtotal.add(grossLineSubtotal);
            discountTotal = discountTotal.add(itemDiscountTotal);
            taxTotal = taxTotal.add(taxAmount);
            total = total.add(lineTotal);
        }

        incomingOrder.setPvpSubtotal(pvpSubtotal.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        incomingOrder.setDiscountTotal(discountTotal.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        incomingOrder.setTaxTotal(taxTotal.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        incomingOrder.setTotal(total.setScale(MONEY_SCALE, RoundingMode.HALF_UP));

        log.info("[{}] Order totals subtotal={} discount={} tax={} total={}",
                SERVICE,
                pvpSubtotal,
                discountTotal,
                taxTotal,
                total);

        Order savedOrder = orderRepository.save(incomingOrder);

        String orderNumber = "ORD-" + String.format("%06d", savedOrder.getId());
        savedOrder.setOrderNumber(orderNumber);

        Order finalOrder = orderRepository.save(savedOrder);

        log.info("[{}] Order created successfully orderId={} orderNumber={}",
                SERVICE,
                finalOrder.getId(),
                finalOrder.getOrderNumber());

        return new OrderCreationResult(finalOrder, true);
    }

    public OrderResponseDto toResponseDto(Order order) {
        return OrderMapper.toDto(order);
    }
}
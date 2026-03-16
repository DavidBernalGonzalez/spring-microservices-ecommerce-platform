package com.microservice.order.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.microservice.order.clients.InventoryClient;
import com.microservice.order.clients.InventoryReserveRequest;
import com.microservice.order.clients.ProductClient;
import com.microservice.order.clients.ProductResponse;
import com.microservice.order.dto.request.OrderRequestDto;
import com.microservice.order.exceptions.InsufficientStockException;
import com.microservice.order.exceptions.ProductNotAvailableForOrderException;
import com.microservice.order.dto.response.OrderResponseDto;
import com.microservice.order.entities.Order;
import com.microservice.order.entities.OrderItem;
import com.microservice.order.mapper.OrderMapper;
import com.microservice.order.repositories.OrderRepository;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrderService {

    private static final String SERVICE = "order-service";

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final int MONEY_SCALE = 2;

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;

    public OrderService(OrderRepository orderRepository, ProductClient productClient, InventoryClient inventoryClient) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
        this.inventoryClient = inventoryClient;
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

            if (!"ACTIVE".equals(product.getStatus())) {

                log.warn("[{}] Product not available for purchase productId={} status={}", SERVICE, item.getProductId(), product.getStatus());

                throw new ProductNotAvailableForOrderException(item.getProductId(), product.getStatus());
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

        if (incomingOrder.getIdempotencyKey() != null && !incomingOrder.getIdempotencyKey().isBlank()) {
            Optional<Order> existingOrder =
                    orderRepository.findWithItemsByIdempotencyKey(incomingOrder.getIdempotencyKey());

            if (existingOrder.isPresent()) {
                log.info("[{}] Idempotent request detected returning existing order orderId={}",
                        SERVICE,
                        existingOrder.get().getId());

                return new OrderCreationResult(existingOrder.get(), false);
            }
        }

        String reserveRef = incomingOrder.getIdempotencyKey() != null && !incomingOrder.getIdempotencyKey().isBlank()
                ? incomingOrder.getIdempotencyKey()
                : "order-" + UUID.randomUUID();

        List<ReservedItem> reservedItems = new ArrayList<>();
        try {
            for (OrderItem item : incomingOrder.getOrderItems()) {
                InventoryReserveRequest reserveRequest = InventoryReserveRequest.builder()
                        .referenceType("ORDER")
                        .referenceNumber(reserveRef)
                        .quantity(item.getQuantity())
                        .reason("Order creation")
                        .build();

                try {
                    inventoryClient.reserveStock(item.getProductId(), reserveRequest);
                    reservedItems.add(new ReservedItem(item.getProductId(), item.getQuantity()));
                    log.info("[{}] Stock reserved productId={} quantity={}", SERVICE, item.getProductId(), item.getQuantity());
                } catch (FeignException e) {
                    String detail = switch (e.status()) {
                        case 404 -> "Inventory not found for product";
                        case 400 -> parseInventoryError(e);
                        default -> "Inventory service error (status " + e.status() + ")";
                    };
                    throw new InsufficientStockException(item.getProductId(), item.getQuantity(), detail);
                }
            }
        } catch (InsufficientStockException e) {
            releaseReservedStock(reservedItems, reserveRef);
            throw e;
        }

        log.info("[{}] Order totals subtotal={} discount={} tax={} total={}",
                SERVICE,
                pvpSubtotal,
                discountTotal,
                taxTotal,
                total);

        try {
            Order savedOrder = orderRepository.save(incomingOrder);

            String orderNumber = "ORD-" + String.format("%06d", savedOrder.getId());
            savedOrder.setOrderNumber(orderNumber);

            Order finalOrder = orderRepository.save(savedOrder);

            log.info("[{}] Order created successfully orderId={} orderNumber={}",
                    SERVICE,
                    finalOrder.getId(),
                    finalOrder.getOrderNumber());

            return new OrderCreationResult(finalOrder, true);
        } catch (Exception e) {
            releaseReservedStock(reservedItems, reserveRef);
            throw e;
        }
    }

    private void releaseReservedStock(List<ReservedItem> reservedItems, String reserveRef) {
        for (int i = reservedItems.size() - 1; i >= 0; i--) {
            ReservedItem ri = reservedItems.get(i);
            try {
                InventoryReserveRequest releaseRequest = InventoryReserveRequest.builder()
                        .referenceType("ORDER")
                        .referenceNumber(reserveRef)
                        .quantity(ri.quantity())
                        .reason("Order creation failed - releasing reserved stock")
                        .build();
                inventoryClient.releaseStock(ri.productId(), releaseRequest);
                log.info("[{}] Stock released productId={} quantity={}", SERVICE, ri.productId(), ri.quantity());
            } catch (Exception ex) {
                log.error("[{}] Failed to release stock productId={} quantity={}", SERVICE, ri.productId(), ri.quantity(), ex);
            }
        }
    }

    private String parseInventoryError(FeignException ex) {
        if (ex.contentUTF8() != null && ex.contentUTF8().contains("Insufficient")) {
            return "Insufficient available stock";
        }
        return "Inventory service error (status " + ex.status() + ")";
    }

    private record ReservedItem(Long productId, Integer quantity) {}

    public OrderResponseDto toResponseDto(Order order) {
        return OrderMapper.toDto(order);
    }
}
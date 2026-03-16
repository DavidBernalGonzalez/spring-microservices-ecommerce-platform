package com.microservice.order.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.microservice.order.clients.InventoryClient;
import com.microservice.order.clients.InventoryReserveRequest;
import com.microservice.order.clients.ProductClient;
import com.microservice.order.clients.ProductResponse;
import com.microservice.order.dto.request.OrderItemRequestDto;
import com.microservice.order.dto.request.OrderRequestDto;
import com.microservice.order.services.OrderService;

/**
 * Contract test: validates Order service works with Product API response shape
 * defined in contracts/product-api.yaml.
 * <p>
 * If Product API changes and breaks the contract, this test must be updated.
 */
@SpringBootTest
@ActiveProfiles("test")
class OrderProductContractTest {

    @Autowired
    private OrderService orderService;

    @MockitoBean
    private ProductClient productClient;

    @MockitoBean
    private InventoryClient inventoryClient;

    @Test
    @DisplayName("Order creation succeeds when Product returns contract-compliant response")
    void shouldCreateOrderWhenProductReturnsContractCompliantResponse() {
        // Contract: Product must have id, name, price, taxRate, status (see contracts/product-api.yaml)
        ProductResponse product = new ProductResponse();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("99.99"));
        product.setTaxRate(new BigDecimal("0.21"));
        product.setStatus("ACTIVE");

        when(productClient.getProductById(1L)).thenReturn(product);

        // Stub inventory reserve (contract: POST /inventory/{productId}/reserve returns 200)
        when(inventoryClient.reserveStock(anyLong(), org.mockito.ArgumentMatchers.any(InventoryReserveRequest.class)))
                .thenReturn(new Object());

        OrderRequestDto request = OrderRequestDto.builder()
                .idempotencyKey("contract-test-001")
                .orderItems(List.of(
                        OrderItemRequestDto.builder()
                                .productId(1L)
                                .quantity(2)
                                .discount(BigDecimal.ZERO)
                                .build()))
                .build();

        var result = orderService.create(request);

        assertThat(result.getOrder()).isNotNull();
        assertThat(result.getOrder().getId()).isNotNull();
        assertThat(result.getOrder().getOrderItems()).hasSize(1);
        assertThat(result.getOrder().getOrderItems().get(0).getProductName()).isEqualTo("Test Product");
        assertThat(result.getOrder().getOrderItems().get(0).getUnitPrice()).isEqualByComparingTo("99.99");
    }
}

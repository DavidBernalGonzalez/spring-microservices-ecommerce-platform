package com.microservice.inventory.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.inventory.dto.request.CreateInventoryRequest;
import com.microservice.inventory.dto.request.StockAdjustmentRequest;
import com.microservice.inventory.dto.response.InventoryResponse;
import com.microservice.inventory.enums.InventoryReferenceType;
import com.microservice.inventory.services.InventoryService;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private InventoryController inventoryController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(inventoryController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Nested
    @DisplayName("GET /api/v1/inventory")
    class GetAll {

        @Test
        @DisplayName("returns 200 with paginated inventory")
        void returns200WithPaginatedInventory() throws Exception {
            InventoryResponse inv = InventoryResponse.builder()
                    .productId(1L)
                    .availableStock(10)
                    .reservedStock(0)
                    .build();

            when(inventoryService.findAll(any()))
                    .thenReturn(new PageImpl<>(List.of(inv), PageRequest.of(0, 20), 1));

            mockMvc.perform(get("/api/v1/inventory"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].productId").value(1))
                    .andExpect(jsonPath("$.content[0].availableStock").value(10));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/inventory/{productId}")
    class GetByProductId {

        @Test
        @DisplayName("returns 200 with inventory when found")
        void returns200WithInventoryWhenFound() throws Exception {
            InventoryResponse inv = InventoryResponse.builder()
                    .productId(1L)
                    .availableStock(15)
                    .reservedStock(0)
                    .build();

            when(inventoryService.findByProductId(1L)).thenReturn(inv);

            mockMvc.perform(get("/api/v1/inventory/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.productId").value(1))
                    .andExpect(jsonPath("$.availableStock").value(15));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/inventory")
    class Create {

        @Test
        @DisplayName("returns 201 when inventory is created")
        void returns201WhenInventoryIsCreated() throws Exception {
            CreateInventoryRequest request = new CreateInventoryRequest();
            request.setProductId(1L);
            request.setInitialStock(100);

            InventoryResponse response = InventoryResponse.builder()
                    .productId(1L)
                    .availableStock(100)
                    .reservedStock(0)
                    .build();

            when(inventoryService.create(eq(1L), eq(100))).thenReturn(response);

            mockMvc.perform(post("/api/v1/inventory")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.productId").value(1))
                    .andExpect(jsonPath("$.availableStock").value(100));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/inventory/{productId}/reserve")
    class Reserve {

        @Test
        @DisplayName("returns 200 when stock is reserved")
        void returns200WhenStockIsReserved() throws Exception {
            StockAdjustmentRequest request = StockAdjustmentRequest.builder()
                    .referenceType(InventoryReferenceType.ORDER)
                    .referenceNumber("ORD-001")
                    .quantity(5)
                    .reason("Order creation")
                    .build();

            InventoryResponse response = InventoryResponse.builder()
                    .productId(1L)
                    .availableStock(5)
                    .reservedStock(5)
                    .build();

            when(inventoryService.reserveStock(eq(1L), eq(InventoryReferenceType.ORDER),
                    eq("ORD-001"), eq(5), eq("Order creation")))
                    .thenReturn(response);

            mockMvc.perform(post("/api/v1/inventory/1/reserve")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.reservedStock").value(5));
        }
    }
}

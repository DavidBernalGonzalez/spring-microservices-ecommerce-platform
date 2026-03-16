package com.microservice.product.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
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
import com.microservice.product.dto.request.ProductRequestDto;
import com.microservice.product.dto.response.ProductResponseDto;
import com.microservice.product.enums.ProductStatus;
import com.microservice.product.services.ProductService;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Nested
    @DisplayName("GET /api/v1/products")
    class GetAll {

        @Test
        @DisplayName("returns 200 with paginated products")
        void returns200WithPaginatedProducts() throws Exception {
            ProductResponseDto product = ProductResponseDto.builder()
                    .id(1L)
                    .sku("SKU-001")
                    .name("Test Product")
                    .price(BigDecimal.valueOf(99.99))
                    .status(ProductStatus.ACTIVE)
                    .build();

            when(productService.findAll(any(), eq(null), eq(null)))
                    .thenReturn(new PageImpl<>(List.of(product), PageRequest.of(0, 20), 1));

            mockMvc.perform(get("/api/v1/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].name").value("Test Product"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products/{id}")
    class GetById {

        @Test
        @DisplayName("returns 200 with product when found")
        void returns200WithProductWhenFound() throws Exception {
            ProductResponseDto product = ProductResponseDto.builder()
                    .id(1L)
                    .sku("SKU-001")
                    .name("Test Product")
                    .price(BigDecimal.valueOf(99.99))
                    .status(ProductStatus.ACTIVE)
                    .build();

            when(productService.findById(1L)).thenReturn(product);

            mockMvc.perform(get("/api/v1/products/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Test Product"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/products")
    class Create {

        @Test
        @DisplayName("returns 201 when product is created")
        void returns201WhenProductIsCreated() throws Exception {
            ProductRequestDto request = ProductRequestDto.builder()
                    .sku("SKU-NEW")
                    .name("New Product")
                    .price(BigDecimal.valueOf(49.99))
                    .categoryId(1L)
                    .build();

            ProductResponseDto response = ProductResponseDto.builder()
                    .id(1L)
                    .sku("SKU-NEW")
                    .name("New Product")
                    .price(BigDecimal.valueOf(49.99))
                    .status(ProductStatus.ACTIVE)
                    .build();

            when(productService.create(any())).thenReturn(response);

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/v1/products/1")))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("New Product"));
        }

        @Test
        @DisplayName("returns 400 when sku is blank")
        void returns400WhenSkuIsBlank() throws Exception {
            ProductRequestDto request = ProductRequestDto.builder()
                    .sku("")
                    .name("Product")
                    .price(BigDecimal.valueOf(10))
                    .categoryId(1L)
                    .build();

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/products/{id}")
    class Delete {

        @Test
        @DisplayName("returns 204 when product is deleted")
        void returns204WhenProductIsDeleted() throws Exception {
            mockMvc.perform(delete("/api/v1/products/1"))
                    .andExpect(status().isNoContent());
        }
    }
}

package com.microservice.product.dto.response;

import java.math.BigDecimal;

import com.microservice.product.enums.ProductStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponseDto {

    private Long id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;

    private Long categoryId;
    private String categoryName;

    private ProductStatus status;
}
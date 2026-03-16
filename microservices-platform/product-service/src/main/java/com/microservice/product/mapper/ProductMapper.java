package com.microservice.product.mapper;

import java.math.BigDecimal;

import com.microservice.product.dto.request.ProductRequestDto;
import com.microservice.product.dto.response.ProductResponseDto;
import com.microservice.product.entities.Category;
import com.microservice.product.entities.Product;
import com.microservice.product.enums.ProductStatus;

public class ProductMapper {

    private ProductMapper() {
    }

    public static Product toEntity(ProductRequestDto dto, Category category) {
        if (dto == null) {
            return null;
        }

        ProductStatus status = dto.getStatus() != null
                ? dto.getStatus()
                : ProductStatus.INACTIVE;

        return Product.builder()
                .sku(dto.getSku())
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .category(category)
                .status(status)
                .build();
    }

    public static void updateEntity(ProductRequestDto dto, Product entity, Category category) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setSku(dto.getSku());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setCategory(category);

        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
    }

    public static ProductResponseDto toDto(Product product) {
        if (product == null) {
            return null;
        }

        BigDecimal taxRate = product.getCategory() != null && product.getCategory().getTaxRate() != null
                ? product.getCategory().getTaxRate()
                : BigDecimal.ZERO;

        return ProductResponseDto.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .taxRate(taxRate)
                .status(product.getStatus())
                .build();
    }
}
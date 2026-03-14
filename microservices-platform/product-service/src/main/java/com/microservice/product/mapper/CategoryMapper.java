package com.microservice.product.mapper;

import com.microservice.product.dto.request.CategoryRequestDto;
import com.microservice.product.dto.response.CategoryResponseDto;
import com.microservice.product.entities.Category;

public class CategoryMapper {

    private CategoryMapper() {
    }

    public static Category toEntity(CategoryRequestDto dto) {
        if (dto == null) {
            return null;
        }

        return Category.builder()
                .name(dto.getName())
                .taxRate(dto.getTaxRate())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();
    }

    public static void updateEntity(CategoryRequestDto dto, Category entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setName(dto.getName());
        entity.setTaxRate(dto.getTaxRate());
        if (dto.getActive() != null) {
            entity.setActive(dto.getActive());
        }
    }

    public static CategoryResponseDto toDto(Category category) {
        if (category == null) {
            return null;
        }

        return CategoryResponseDto.builder()
                .id(category.getId())
                .name(category.getName())
                .taxRate(category.getTaxRate())
                .active(category.getActive())
                .build();
    }
}
package com.microservice.product.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.microservice.product.dto.request.CategoryRequestDto;
import com.microservice.product.dto.response.CategoryResponseDto;
import com.microservice.product.services.CategoryService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private static final String SERVICE = "product-service";

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getAll() {

        log.info("[{}] GET /api/categories - fetching all categories", SERVICE);

        List<CategoryResponseDto> categories = categoryService.findAll();

        log.info("[{}] GET /api/categories - returned {} categories", SERVICE, categories.size());

        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> getById(@PathVariable Long id) {

        log.info("[{}] GET /api/categories/{} - fetching category", SERVICE, id);

        CategoryResponseDto category = categoryService.findById(id);

        log.info("[{}] Category found id={} name={}", SERVICE, category.getId(), category.getName());

        return ResponseEntity.ok(category);
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDto> create(@Valid @RequestBody CategoryRequestDto request) {

        log.info("[{}] POST /api/categories - creating category name={}", SERVICE, request.getName());

        CategoryResponseDto created = categoryService.create(request);

        log.info("[{}] Category created id={} name={} taxRate={}",
                SERVICE,
                created.getId(),
                created.getName(),
                created.getTaxRate());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .location(URI.create("/api/categories/" + created.getId()))
                .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequestDto request) {

        log.info("[{}] PUT /api/categories/{} - updating category", SERVICE, id);

        CategoryResponseDto updated = categoryService.update(id, request);

        log.info("[{}] Category updated id={} name={} taxRate={}",
                SERVICE,
                updated.getId(),
                updated.getName(),
                updated.getTaxRate());

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        log.info("[{}] DELETE /api/categories/{} - deleting category", SERVICE, id);

        categoryService.delete(id);

        log.info("[{}] Category deleted id={}", SERVICE, id);

        return ResponseEntity.noContent().build();
    }
}
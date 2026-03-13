package com.microservice.product.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.microservice.product.entities.Category;
import com.microservice.product.repository.CategoryRepository;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private static final String SERVICE = "product-service";

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public List<Category> getAll() {

        log.info("[{}] GET /api/categories - fetching all categories", SERVICE);

        List<Category> categories = categoryRepository.findAll();

        log.info("[{}] GET /api/categories - returned {} categories", SERVICE, categories.size());

        return categories;
    }

    @GetMapping("/{id}")
    public Category getById(@PathVariable Long id) {

        log.info("[{}] GET /api/categories/{} - fetching category", SERVICE, id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[{}] Category not found id={}", SERVICE, id);
                    return new RuntimeException("Category not found with id: " + id);
                });

        log.info("[{}] Category found id={} name={}", SERVICE, category.getId(), category.getName());

        return category;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Category create(@Valid @RequestBody Category category) {

        log.info("[{}] POST /api/categories - creating category name={}", SERVICE, category.getName());

        categoryRepository.findByName(category.getName())
                .ifPresent(c -> {
                    log.warn("[{}] Category already exists name={}", SERVICE, category.getName());
                    throw new RuntimeException("Category already exists: " + category.getName());
                });

        Category saved = categoryRepository.save(category);

        log.info("[{}] Category created id={} name={} taxRate={}",
                SERVICE,
                saved.getId(),
                saved.getName(),
                saved.getTaxRate());

        return saved;
    }

    @PutMapping("/{id}")
    public Category update(@PathVariable Long id, @Valid @RequestBody Category categoryRequest) {

        log.info("[{}] PUT /api/categories/{} - updating category", SERVICE, id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[{}] Category not found for update id={}", SERVICE, id);
                    return new RuntimeException("Category not found with id: " + id);
                });

        category.setName(categoryRequest.getName());
        category.setTaxRate(categoryRequest.getTaxRate());

        Category updated = categoryRepository.save(category);

        log.info("[{}] Category updated id={} name={} taxRate={}",
                SERVICE,
                updated.getId(),
                updated.getName(),
                updated.getTaxRate());

        return updated;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {

        log.info("[{}] DELETE /api/categories/{} - deleting category", SERVICE, id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[{}] Category not found for delete id={}", SERVICE, id);
                    return new RuntimeException("Category not found with id: " + id);
                });

        categoryRepository.delete(category);

        log.info("[{}] Category deleted id={} name={}",
                SERVICE,
                category.getId(),
                category.getName());
    }
}
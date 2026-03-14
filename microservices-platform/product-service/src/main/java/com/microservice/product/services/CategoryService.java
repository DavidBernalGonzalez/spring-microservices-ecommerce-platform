package com.microservice.product.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.microservice.product.dto.request.CategoryRequestDto;
import com.microservice.product.dto.response.CategoryResponseDto;
import com.microservice.product.entities.Category;
import com.microservice.product.mapper.CategoryMapper;
import com.microservice.product.repository.CategoryRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CategoryService {

    private static final String SERVICE = "product-service";

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponseDto> findAll() {
        log.info("[{}] Fetching all categories", SERVICE);

        List<CategoryResponseDto> categories = categoryRepository.findAll()
                .stream()
                .map(CategoryMapper::toDto)
                .toList();

        log.info("[{}] Returned {} categories", SERVICE, categories.size());

        return categories;
    }

    public CategoryResponseDto findById(Long id) {
        log.info("[{}] Fetching category id={}", SERVICE, id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[{}] Category not found id={}", SERVICE, id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with id: " + id);
                });

        log.info("[{}] Category found id={} name={}", SERVICE, category.getId(), category.getName());

        return CategoryMapper.toDto(category);
    }

    public CategoryResponseDto create(CategoryRequestDto request) {
        log.info("[{}] Creating category name={}", SERVICE, request.getName());

        categoryRepository.findByName(request.getName())
                .ifPresent(c -> {
                    log.warn("[{}] Category already exists name={}", SERVICE, request.getName());
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Category already exists: " + request.getName());
                });

        Category category = CategoryMapper.toEntity(request);
        Category saved = categoryRepository.save(category);

        log.info("[{}] Category created id={} name={} taxRate={}",
                SERVICE,
                saved.getId(),
                saved.getName(),
                saved.getTaxRate());

        return CategoryMapper.toDto(saved);
    }

    public CategoryResponseDto update(Long id, CategoryRequestDto request) {
        log.info("[{}] Updating category id={}", SERVICE, id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[{}] Category not found for update id={}", SERVICE, id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with id: " + id);
                });

        CategoryMapper.updateEntity(request, category);
        Category updated = categoryRepository.save(category);

        log.info("[{}] Category updated id={} name={} taxRate={}",
                SERVICE,
                updated.getId(),
                updated.getName(),
                updated.getTaxRate());

        return CategoryMapper.toDto(updated);
    }

    public void delete(Long id) {
        log.info("[{}] Deleting category id={}", SERVICE, id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[{}] Category not found for delete id={}", SERVICE, id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with id: " + id);
                });

        categoryRepository.delete(category);

        log.info("[{}] Category deleted id={} name={}",
                SERVICE,
                category.getId(),
                category.getName());
    }
}

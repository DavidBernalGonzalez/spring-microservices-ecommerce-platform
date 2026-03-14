package com.microservice.product.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.microservice.product.dto.request.ProductRequestDto;
import com.microservice.product.dto.response.ProductResponseDto;
import com.microservice.product.entities.Category;
import com.microservice.product.entities.Product;
import com.microservice.product.enums.ProductStatus;
import com.microservice.product.mapper.ProductMapper;
import com.microservice.product.repository.CategoryRepository;
import com.microservice.product.repository.ProductRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductService {

    private static final String SERVICE = "product-service";

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<ProductResponseDto> findAll() {

        log.info("[{}] Fetching all products (not deleted)", SERVICE);

        List<ProductResponseDto> products = productRepository.findByDeletedFalse()
                .stream()
                .map(ProductMapper::toDto)
                .toList();

        log.info("[{}] Returned {} products", SERVICE, products.size());

        return products;
    }

    public ProductResponseDto findById(Long id) {

        log.info("[{}] Fetching product id={}", SERVICE, id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[{}] Product not found id={}", SERVICE, id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
                });

        if (Boolean.TRUE.equals(product.getDeleted())) {

            log.warn("[{}] Product requested but marked deleted id={}", SERVICE, id);

            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }

        log.info("[{}] Product found id={} sku={} name={}",
                SERVICE,
                product.getId(),
                product.getSku(),
                product.getName());

        return ProductMapper.toDto(product);
    }

    public ProductResponseDto create(ProductRequestDto request) {

        log.info("[{}] Creating product sku={} name={}",
                SERVICE,
                request.getSku(),
                request.getName());

        if (productRepository.existsBySkuAndDeletedFalse(request.getSku())) {

            log.warn("[{}] Product SKU already exists sku={}", SERVICE, request.getSku());

            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product SKU already exists");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Category not found: " + request.getCategoryId()
                ));

        Product product = ProductMapper.toEntity(request, category);

        Product saved = productRepository.save(product);

        log.info("[{}] Product created id={} sku={} name={}",
                SERVICE,
                saved.getId(),
                saved.getSku(),
                saved.getName());

        return ProductMapper.toDto(saved);
    }

    public ProductResponseDto update(Long id, ProductRequestDto request) {

        log.info("[{}] Updating product id={}", SERVICE, id);

        Product existing = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[{}] Product not found for update id={}", SERVICE, id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
                });

        if (Boolean.TRUE.equals(existing.getDeleted())) {

            log.warn("[{}] Attempt to update deleted product id={}", SERVICE, id);

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product is deleted");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Category not found: " + request.getCategoryId()
                ));

        ProductMapper.updateEntity(request, existing, category);

        Product updated = productRepository.save(existing);

        log.info("[{}] Product updated id={} sku={} name={}",
                SERVICE,
                updated.getId(),
                updated.getSku(),
                updated.getName());

        return ProductMapper.toDto(updated);
    }

    public void delete(Long id) {

        log.info("[{}] Deleting product id={}", SERVICE, id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[{}] Product not found for delete id={}", SERVICE, id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
                });

        product.setDeleted(true);
        product.setStatus(ProductStatus.INACTIVE);

        productRepository.save(product);

        log.info("[{}] Product soft-deleted id={} sku={}",
                SERVICE,
                product.getId(),
                product.getSku());
    }
}
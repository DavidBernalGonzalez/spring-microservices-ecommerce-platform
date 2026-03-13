package com.microservice.product.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.microservice.product.entities.Product;
import com.microservice.product.enums.ProductStatus;
import com.microservice.product.repository.ProductRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductService {

    private static final String SERVICE = "product-service";

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAll() {

        log.info("[{}] Fetching all products (not deleted)", SERVICE);

        List<Product> products = productRepository.findByDeletedFalse();

        log.info("[{}] Returned {} products", SERVICE, products.size());

        return products;
    }

    public Product findById(Long id) {

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

        return product;
    }

    public Product create(Product product) {

        log.info("[{}] Creating product sku={} name={}",
                SERVICE,
                product.getSku(),
                product.getName());

        if (productRepository.existsBySkuAndDeletedFalse(product.getSku())) {

            log.warn("[{}] Product SKU already exists sku={}", SERVICE, product.getSku());

            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product SKU already exists");
        }

        Product saved = productRepository.save(product);

        log.info("[{}] Product created id={} sku={} name={}",
                SERVICE,
                saved.getId(),
                saved.getSku(),
                saved.getName());

        return saved;
    }

    public Product update(Long id, Product product) {

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

        existing.setSku(product.getSku());
        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setStatus(product.getStatus());

        Product updated = productRepository.save(existing);

        log.info("[{}] Product updated id={} sku={} name={}",
                SERVICE,
                updated.getId(),
                updated.getSku(),
                updated.getName());

        return updated;
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
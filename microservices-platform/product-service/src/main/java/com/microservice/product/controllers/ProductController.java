package com.microservice.product.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.microservice.product.entities.Product;
import com.microservice.product.services.ProductService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final String SERVICE = "product-service";

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAll() {

        log.info("[{}] GET /api/products - fetching all products", SERVICE);

        List<Product> products = productService.findAll();

        log.info("[{}] GET /api/products - returned {} products", SERVICE, products.size());

        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id) {

        log.info("[{}] GET /api/products/{} - fetching product", SERVICE, id);

        Product product = productService.findById(id);

        log.info("[{}] Product found id={} name={}", SERVICE, product.getId(), product.getName());

        return ResponseEntity.ok(product);
    }

    @PostMapping
    public ResponseEntity<Product> create(@Valid @RequestBody Product product) {

        log.info("[{}] POST /api/products - creating product sku={} name={}",
                SERVICE,
                product.getSku(),
                product.getName());

        Product created = productService.create(product);

        log.info("[{}] Product created id={} sku={} name={}",
                SERVICE,
                created.getId(),
                created.getSku(),
                created.getName());

        return ResponseEntity
                .created(URI.create("/api/products/" + created.getId()))
                .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(
            @PathVariable Long id,
            @Valid @RequestBody Product product) {

        log.info("[{}] PUT /api/products/{} - updating product", SERVICE, id);

        Product updated = productService.update(id, product);

        log.info("[{}] Product updated id={} name={}",
                SERVICE,
                updated.getId(),
                updated.getName());

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        log.info("[{}] DELETE /api/products/{} - deleting product", SERVICE, id);

        productService.delete(id);

        log.info("[{}] Product deleted id={}", SERVICE, id);

        return ResponseEntity.noContent().build();
    }
}
package com.microservice.product.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.microservice.product.entities.Product;
import com.microservice.product.enums.ProductStatus;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySkuAndDeletedFalse(String sku);

    Page<Product> findByDeletedFalse(Pageable pageable);

    Page<Product> findByDeletedFalseAndStatus(ProductStatus status, Pageable pageable);

    Page<Product> findByDeletedFalseAndCategory_Id(Long categoryId, Pageable pageable);

    Page<Product> findByDeletedFalseAndStatusAndCategory_Id(ProductStatus status, Long categoryId, Pageable pageable);
}
package com.microservice.product.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.microservice.product.entities.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
	boolean existsBySkuAndDeletedFalse(String sku);
	List<Product> findByDeletedFalse();
}

package com.microservice.product.config;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.microservice.product.entities.Category;
import com.microservice.product.repository.CategoryRepository;

@Configuration
public class CategoryDataLoader {

    @Bean
    @Order(1)
    CommandLineRunner loadCategories(CategoryRepository categoryRepository) {
        return args -> {

            createIfNotExists(categoryRepository, "ELECTRONICS", "0.21");
            createIfNotExists(categoryRepository, "CLOTHING", "0.21");
            createIfNotExists(categoryRepository, "HOME", "0.21");

            createIfNotExists(categoryRepository, "BOOKS", "0.04");

            createIfNotExists(categoryRepository, "FOOD_BASIC", "0.04");
            createIfNotExists(categoryRepository, "FOOD_REDUCED", "0.10");

            createIfNotExists(categoryRepository, "PHARMACY", "0.04");

            createIfNotExists(categoryRepository, "LAPTOPS", "0.21");
            createIfNotExists(categoryRepository, "SMARTPHONES", "0.21");
            createIfNotExists(categoryRepository, "AUDIO", "0.21");
            createIfNotExists(categoryRepository, "MONITORS", "0.21");
            createIfNotExists(categoryRepository, "ACCESSORIES", "0.21");

        };
    }

    private void createIfNotExists(CategoryRepository repo, String name, String taxRate) {

        repo.findByName(name).orElseGet(() -> {

            Category category = Category.builder()
                    .name(name)
                    .taxRate(new BigDecimal(taxRate))
                    .build();

            return repo.save(category);
        });
    }
}
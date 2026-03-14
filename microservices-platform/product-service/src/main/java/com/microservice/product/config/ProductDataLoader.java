package com.microservice.product.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.microservice.product.entities.Category;
import com.microservice.product.entities.Product;
import com.microservice.product.enums.ProductStatus;
import com.microservice.product.repository.CategoryRepository;
import com.microservice.product.repository.ProductRepository;

@Configuration
public class ProductDataLoader {

    @Bean
    @Order(2)
    CommandLineRunner loadProducts(ProductRepository productRepository, CategoryRepository categoryRepository) {
        return args -> {

            Category laptops = categoryRepository.findByName("LAPTOPS").orElseThrow();
            Category smartphones = categoryRepository.findByName("SMARTPHONES").orElseThrow();
            Category audio = categoryRepository.findByName("AUDIO").orElseThrow();
            Category monitors = categoryRepository.findByName("MONITORS").orElseThrow();
            Category accessories = categoryRepository.findByName("ACCESSORIES").orElseThrow();

            List<ProductRecord> products = List.of(
                    new ProductRecord("SKU-LAPTOP-005", "Laptop Studio Pro", "Laptop profesional con pantalla OLED y gran rendimiento", new BigDecimal("2199.99"), laptops),
                    new ProductRecord("SKU-LAPTOP-006", "Travel Laptop 13", "Portátil ultraligero para viajes y trabajo remoto", new BigDecimal("1099.99"), laptops),
                    new ProductRecord("SKU-LAPTOP-007", "Developer Laptop X", "Laptop para programación con 32GB RAM", new BigDecimal("1999.99"), laptops),
                    new ProductRecord("SKU-LAPTOP-008", "Laptop Essential", "Laptop económica para tareas básicas", new BigDecimal("699.99"), laptops),
                    new ProductRecord("SKU-LAPTOP-009", "Laptop Flex 2in1", "Laptop convertible 2 en 1 con pantalla táctil", new BigDecimal("1299.99"), laptops),
                    new ProductRecord("SKU-LAPTOP-010", "Workstation Laptop Z", "Laptop workstation para edición de vídeo", new BigDecimal("2499.99"), laptops),

                    new ProductRecord("SKU-PHONE-005", "Smartphone PowerMax", "Smartphone con batería de larga duración", new BigDecimal("799.99"), smartphones),
                    new ProductRecord("SKU-PHONE-006", "Smartphone Vision", "Smartphone con triple cámara y 5G", new BigDecimal("899.99"), smartphones),
                    new ProductRecord("SKU-PHONE-007", "Smartphone Compact Plus", "Smartphone compacto de gama media", new BigDecimal("549.99"), smartphones),
                    new ProductRecord("SKU-PHONE-008", "Smartphone Outdoor", "Smartphone resistente al agua y polvo", new BigDecimal("649.99"), smartphones),
                    new ProductRecord("SKU-PHONE-009", "Smartphone Elite", "Smartphone premium con pantalla AMOLED", new BigDecimal("1199.99"), smartphones),
                    new ProductRecord("SKU-PHONE-010", "Smartphone Basic", "Smartphone económico para uso diario", new BigDecimal("299.99"), smartphones),

                    new ProductRecord("SKU-AUDIO-005", "Sport Wireless Earbuds", "Auriculares inalámbricos deportivos", new BigDecimal("99.99"), audio),
                    new ProductRecord("SKU-AUDIO-006", "Studio Headphones Pro", "Auriculares estudio para audio profesional", new BigDecimal("349.99"), audio),
                    new ProductRecord("SKU-AUDIO-007", "Smart Speaker Home", "Altavoz inteligente con asistente de voz", new BigDecimal("149.99"), audio),
                    new ProductRecord("SKU-AUDIO-008", "Mini Bluetooth Speaker", "Altavoz portátil compacto", new BigDecimal("59.99"), audio),
                    new ProductRecord("SKU-AUDIO-009", "Noise Cancel Headphones", "Auriculares over-ear con cancelación activa", new BigDecimal("279.99"), audio),
                    new ProductRecord("SKU-AUDIO-010", "Soundbar Cinema", "Barra de sonido para cine en casa", new BigDecimal("399.99"), audio),

                    new ProductRecord("SKU-MONITOR-005", "Monitor Office 22\"", "Monitor FullHD 22 pulgadas para oficina", new BigDecimal("159.99"), monitors),
                    new ProductRecord("SKU-MONITOR-006", "Monitor 4K 32\"", "Monitor 32 pulgadas 4K profesional", new BigDecimal("799.99"), monitors),
                    new ProductRecord("SKU-MONITOR-007", "Gaming Monitor 240Hz", "Monitor gaming 240Hz", new BigDecimal("499.99"), monitors),
                    new ProductRecord("SKU-MONITOR-008", "Portable Monitor 15\"", "Monitor portátil USB-C para movilidad", new BigDecimal("299.99"), monitors),
                    new ProductRecord("SKU-MONITOR-009", "Curved Ultrawide 34\"", "Monitor curvo 34 pulgadas ultrawide", new BigDecimal("749.99"), monitors),
                    new ProductRecord("SKU-MONITOR-010", "Photo Editing Monitor", "Monitor profesional para fotografía", new BigDecimal("899.99"), monitors),

                    new ProductRecord("SKU-KEYBOARD-003", "Mechanical Keyboard 60%", "Teclado mecánico compacto 60%", new BigDecimal("129.99"), accessories),
                    new ProductRecord("SKU-KEYBOARD-004", "Office Silent Keyboard", "Teclado silencioso para oficina", new BigDecimal("49.99"), accessories),
                    new ProductRecord("SKU-KEYBOARD-005", "Gaming Keyboard RGB Pro", "Teclado gaming retroiluminado", new BigDecimal("179.99"), accessories),
                    new ProductRecord("SKU-KEYBOARD-006", "Wireless Slim Keyboard", "Teclado inalámbrico slim", new BigDecimal("59.99"), accessories),
                    new ProductRecord("SKU-MOUSE-003", "Vertical Ergonomic Mouse", "Ratón vertical ergonómico", new BigDecimal("79.99"), accessories),
                    new ProductRecord("SKU-MOUSE-004", "Gaming Mouse X", "Ratón gaming con DPI ajustable", new BigDecimal("119.99"), accessories),
                    new ProductRecord("SKU-MOUSE-005", "Compact Wireless Mouse", "Ratón inalámbrico compacto", new BigDecimal("39.99"), accessories),
                    new ProductRecord("SKU-MOUSE-006", "Designer Precision Mouse", "Ratón profesional para diseño", new BigDecimal("129.99"), accessories),
                    new ProductRecord("SKU-MOUSEPAD-001", "Gaming Mouse Pad XL", "Alfombrilla gaming XL", new BigDecimal("29.99"), accessories),
                    new ProductRecord("SKU-ACCESSORY-001", "Laptop Stand Pro", "Soporte ergonómico para portátil", new BigDecimal("69.99"), accessories),
                    new ProductRecord("SKU-ACCESSORY-002", "USB-C Hub 8in1", "Hub USB-C multipuerto", new BigDecimal("89.99"), accessories),
                    new ProductRecord("SKU-ACCESSORY-003", "Laptop Cooling Pad", "Base de refrigeración para laptop", new BigDecimal("49.99"), accessories),
                    new ProductRecord("SKU-ACCESSORY-004", "USB-C Dock Station", "Docking station USB-C", new BigDecimal("199.99"), accessories),
                    new ProductRecord("SKU-ACCESSORY-005", "Webcam FullHD Pro", "Webcam FullHD para videollamadas", new BigDecimal("89.99"), accessories),
                    new ProductRecord("SKU-ACCESSORY-006", "Streaming LED Light", "Luz LED para streaming", new BigDecimal("79.99"), accessories)
            );

            for (ProductRecord p : products) {
                createIfNotExists(productRepository, p);
            }
        };
    }

    private void createIfNotExists(ProductRepository repo, ProductRecord p) {
        if (repo.existsBySkuAndDeletedFalse(p.sku())) {
            return;
        }

        Product product = Product.builder()
                .sku(p.sku())
                .name(p.name())
                .description(p.description())
                .price(p.price())
                .category(p.category())
                .status(ProductStatus.ACTIVE)
                .deleted(false)
                .build();

        repo.save(product);
    }

    private record ProductRecord(String sku, String name, String description, BigDecimal price, Category category) {
    }
}

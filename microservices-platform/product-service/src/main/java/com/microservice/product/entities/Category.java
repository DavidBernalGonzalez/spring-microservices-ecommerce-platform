package com.microservice.product.entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "categories")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 80, message = "Category name must be between 2 and 80 characters")
    @Column(nullable = false, unique = true, length = 80)
    private String name;

    @NotNull(message = "Tax rate is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Tax rate must be greater than or equal to 0")
    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 4)
    @Default
    private BigDecimal taxRate = BigDecimal.ZERO;

    @Column(nullable = false)
    @Default
    private Boolean active = true;

    @PrePersist
    public void prePersist() {
        if (this.taxRate == null) {
            this.taxRate = BigDecimal.ZERO;
        }

        if (this.active == null) {
            this.active = true;
        }
    }
}
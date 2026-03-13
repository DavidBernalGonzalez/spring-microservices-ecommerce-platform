package com.microservice.order.entities;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Product id is required")
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 150)
    private String productName;

    @DecimalMin(value = "0.0", inclusive = true, message = "Unit price must be greater than or equal to 0")
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "Discount must be greater than or equal to 0")
    @Column(nullable = false, precision = 12, scale = 2)
    @Default
    private BigDecimal discount = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", inclusive = true, message = "Final price must be greater than or equal to 0")
    @Column(name = "final_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal finalPrice;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(nullable = false)
    private Integer quantity;

    @DecimalMin(value = "0.0", inclusive = true, message = "Line subtotal must be greater than or equal to 0")
    @Column(name = "line_subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineSubtotal;

    @NotNull(message = "Tax rate is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Tax rate must be greater than or equal to 0")
    @Column(name = "tax_rate", nullable = false, precision = 4, scale = 2)
    @Default
    private BigDecimal taxRate = BigDecimal.ZERO;

    @NotNull(message = "Tax amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Tax amount must be greater than or equal to 0")
    @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
    @Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @NotNull(message = "Line total is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Line total must be greater than or equal to 0")
    @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
    @Default
    private BigDecimal lineTotal = BigDecimal.ZERO;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private Order order;

    @PrePersist
    public void prePersist() {
        if (this.discount == null) {
            this.discount = BigDecimal.ZERO;
        }
        if (this.finalPrice == null) {
            this.finalPrice = BigDecimal.ZERO;
        }
        if (this.lineSubtotal == null) {
            this.lineSubtotal = BigDecimal.ZERO;
        }
        if (this.taxRate == null) {
            this.taxRate = BigDecimal.ZERO;
        }
        if (this.taxAmount == null) {
            this.taxAmount = BigDecimal.ZERO;
        }
        if (this.lineTotal == null) {
            this.lineTotal = BigDecimal.ZERO;
        }
    }
}
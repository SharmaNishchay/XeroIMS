package com.darkhex.xeroims.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PricingDTO {

    private Long id;

    @NotNull(message = "Product ID is required")
    private Long productId;

    private String productName;

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    private String supplierName;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price cannot be negative")
    private BigDecimal price;

    private Long userId;

    private Long oauthUserId;

    private Long categoryId; // Added to hold the selected category ID
}
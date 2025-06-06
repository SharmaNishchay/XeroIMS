package com.darkhex.xeroims.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDTO {

    private Long id;

    @NotBlank(message = "Product name cannot be blank")
    private String name;

    @NotBlank(message = "SKU cannot be blank")
    private String sku;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price cannot be negative")
    private BigDecimal price;

    private Integer quantity;

    @NotNull(message = "Category ID is required")
    private Long categoryId;
}


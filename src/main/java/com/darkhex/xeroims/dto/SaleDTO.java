package com.darkhex.xeroims.dto;

import com.darkhex.xeroims.enums.Status;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SaleDTO {

    private Long id;

    private Long pricingId;

    @NotNull(message = "Product ID is required")
    private Long productId;

    private String productName;

    @NotNull(message = "Customer name is required")
    private String customerName;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private BigDecimal price;

    private BigDecimal totalPrice;

    private Status status = Status.PENDING;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate salesDate;

    private Long userId;

    private Long oauthUserId;

    private Long categoryId;
}

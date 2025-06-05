package com.darkhex.xeroims.dto;

import com.darkhex.xeroims.enums.Status;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class SaleDTO {

    private Long id;

    private Long pricingId;

    @NotNull(message = "Product ID is required")
    private Long productId;

    private String productName;

    private Long customerId;

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

    public SaleDTO(Long id, Long productId, String productName, String customerName,
                  Integer quantity, BigDecimal price, BigDecimal totalPrice,
                  LocalDate salesDate, Status status) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.customerName = customerName;
        this.quantity = quantity;
        this.price = price;
        this.totalPrice = totalPrice;
        this.salesDate = salesDate;
        this.status = status;
    }
}

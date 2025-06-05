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
public class PurchaseDTO {

    private Long id;

    private Long pricingId;

    @NotNull(message = "Product ID is required")
    private Long productId;

    private String productName;

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    private String supplierName;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private BigDecimal price;

    private BigDecimal totalPrice;

    private Status status = Status.PENDING;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate purchaseDate;

    private Long userId;

    private Long oauthUserId;

    private Long categoryId;

    public PurchaseDTO(Long id, Long productId, String productName, Long supplierId,
                      String supplierName, Integer quantity, BigDecimal totalPrice,
                      LocalDate purchaseDate, Status status) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.purchaseDate = purchaseDate;
        this.status = status;
    }
}

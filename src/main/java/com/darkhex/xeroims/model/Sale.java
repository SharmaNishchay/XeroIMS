package com.darkhex.xeroims.model;

import com.darkhex.xeroims.enums.Status;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "sales")
@Getter
@Setter
@NoArgsConstructor
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pricing_id", nullable = false)
    private Pricing pricing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer quantity;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private BigDecimal price;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "sales_date", nullable = false)
    private LocalDate salesDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oauth_user_id")
    private OauthUser oauthUser;

    public Sale(Pricing pricing, Product product, String customerName, Integer quantity,
                BigDecimal price, BigDecimal totalPrice, LocalDate salesDate, Status status,
                User user, OauthUser oauthUser) {
        this.pricing = pricing;
        this.product = product;
        this.customerName = customerName;
        this.quantity = quantity;
        this.price = price;
        this.totalPrice = totalPrice;
        this.salesDate = salesDate;
        this.status = status;
        this.user = user;
        this.oauthUser = oauthUser;
    }
}

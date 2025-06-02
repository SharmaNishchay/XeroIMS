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
@Table(name = "purchase")
@Getter
@Setter
@NoArgsConstructor
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pricing_id", nullable = false)
    private Pricing pricing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer quantity;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oauth_user_id")
    private OauthUser oauthUser;

    public Purchase(Pricing pricing, Product product, Supplier supplier, Integer quantity,
                    BigDecimal totalPrice, LocalDate purchaseDate, Status status,
                    User user, OauthUser oauthUser) {
        this.pricing = pricing;
        this.product = product;
        this.supplier = supplier;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.purchaseDate = purchaseDate;
        this.status = status;
        this.user = user;
        this.oauthUser = oauthUser;
    }
}


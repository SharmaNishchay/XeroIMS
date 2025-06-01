package com.darkhex.xeroims.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "pricing",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"product_id", "supplier_id", "user_id"}),
                @UniqueConstraint(columnNames = {"product_id", "supplier_id", "oauth_user_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Pricing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oauth_user_id")
    private OauthUser oauthUser;

    public Pricing(Product product, Supplier supplier, BigDecimal price, User user, OauthUser oauthUser) {
        this.product = product;
        this.supplier = supplier;
        this.price = price;
        this.user = user;
        this.oauthUser = oauthUser;
    }
}
package com.darkhex.xeroims.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "products",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"sku", "user_id"}),
                @UniqueConstraint(columnNames = {"sku", "oauth_user_id"})
        }
)
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String sku;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private BigDecimal price;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oauth_user_id")
    private OauthUser oauthUser;

    public Product() {}

    public Product(String name, String sku, BigDecimal price, Integer quantity,
                   Category category, User user, OauthUser oauthUser) {
        this.name = name;
        this.sku = sku;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
        this.user = user;
        this.oauthUser = oauthUser;
    }
}

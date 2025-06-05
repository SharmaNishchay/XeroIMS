package com.darkhex.xeroims.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String email;

    private String phone;

    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oauth_user_id")
    private OauthUser oauthUser;

    public Customer(String name, String email, String phone, String address, User user, OauthUser oauthUser) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.user = user;
        this.oauthUser = oauthUser;
    }
}

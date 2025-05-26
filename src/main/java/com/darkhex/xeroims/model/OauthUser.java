package com.darkhex.xeroims.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "oauth_users")
@Getter
@Setter
public class OauthUser {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private String id;

    @Column(unique = true, nullable = false)
    private String email;

    private String name;

    public OauthUser() {}

    public OauthUser(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
}

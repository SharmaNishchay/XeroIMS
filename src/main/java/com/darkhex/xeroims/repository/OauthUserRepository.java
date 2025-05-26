package com.darkhex.xeroims.repository;

import com.darkhex.xeroims.model.OauthUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OauthUserRepository extends JpaRepository<OauthUser, String> {
    Optional<OauthUser> findByEmail(String email);
}

package com.darkhex.xeroims.repository;

import com.darkhex.xeroims.model.Purchase;
import com.darkhex.xeroims.model.User;
import com.darkhex.xeroims.model.OauthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    @Query("SELECT p FROM Purchase p " +
            "JOIN FETCH p.product prod " +
            "JOIN FETCH p.supplier sup " +
            "JOIN FETCH p.pricing pr " +
            "WHERE p.user = :user " +
            "ORDER BY p.purchaseDate DESC")
    List<Purchase> findAllByUserOrderByPurchaseDateDesc(@Param("user") User user);

    @Query("SELECT p FROM Purchase p " +
            "JOIN FETCH p.product prod " +
            "JOIN FETCH p.supplier sup " +
            "JOIN FETCH p.pricing pr " +
            "WHERE p.oauthUser = :oauthUser " +
            "ORDER BY p.purchaseDate DESC")
    List<Purchase> findAllByOauthUserOrderByPurchaseDateDesc(@Param("oauthUser") OauthUser oauthUser);
}

package com.darkhex.xeroims.repository;

import com.darkhex.xeroims.model.Sale;
import com.darkhex.xeroims.model.User;
import com.darkhex.xeroims.model.OauthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    @Query("SELECT s FROM Sale s " +
            "JOIN FETCH s.product prod " +
            "JOIN FETCH s.pricing pr " +
            "WHERE s.user = :user " +
            "ORDER BY s.salesDate DESC")
    List<Sale> findAllByUserOrderBySalesDateDesc(@Param("user") User user);

    @Query("SELECT s FROM Sale s " +
            "JOIN FETCH s.product prod " +
            "JOIN FETCH s.pricing pr " +
            "WHERE s.oauthUser = :oauthUser " +
            "ORDER BY s.salesDate DESC")
    List<Sale> findAllByOauthUserOrderBySalesDateDesc(@Param("oauthUser") OauthUser oauthUser);
}

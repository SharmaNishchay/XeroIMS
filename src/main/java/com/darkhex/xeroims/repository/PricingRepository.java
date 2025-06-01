package com.darkhex.xeroims.repository;

import com.darkhex.xeroims.model.Pricing;
import com.darkhex.xeroims.model.User;
import com.darkhex.xeroims.model.OauthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PricingRepository extends JpaRepository<Pricing, Long> {

    @Query("SELECT p FROM Pricing p " +
            "JOIN FETCH p.product prod " +
            "JOIN FETCH p.supplier sup " +
            "WHERE p.user = :user " +
            "ORDER BY p.price ASC")
    List<Pricing> findAllByUserOrderByPriceAsc(@Param("user") User user);

    @Query("SELECT p FROM Pricing p " +
            "JOIN FETCH p.product prod " +
            "JOIN FETCH p.supplier sup " +
            "WHERE p.oauthUser = :oauthUser " +
            "ORDER BY p.price ASC")
    List<Pricing> findAllByOauthUserOrderByPriceAsc(@Param("oauthUser") OauthUser oauthUser);

    @Query("SELECT p FROM Pricing p " +
            "JOIN FETCH p.product prod " +
            "JOIN FETCH p.supplier sup " +
            "WHERE prod.id = :productId AND sup.id = :supplierId AND p.user = :user")
    Optional<Pricing> findByProductIdAndSupplierIdAndUser(@Param("productId") Long productId,
                                                          @Param("supplierId") Long supplierId,
                                                          @Param("user") User user);

    @Query("SELECT p FROM Pricing p " +
            "JOIN FETCH p.product prod " +
            "JOIN FETCH p.supplier sup " +
            "WHERE prod.id = :productId AND sup.id = :supplierId AND p.oauthUser = :oauthUser")
    Optional<Pricing> findByProductIdAndSupplierIdAndOauthUser(@Param("productId") Long productId,
                                                               @Param("supplierId") Long supplierId,
                                                               @Param("oauthUser") OauthUser oauthUser);

    boolean existsByProductIdAndSupplierIdAndUser(Long productId, Long supplierId, User user);

    boolean existsByProductIdAndSupplierIdAndOauthUser(Long productId, Long supplierId, OauthUser oauthUser);

    @Query("SELECT p FROM Pricing p JOIN FETCH p.product prod JOIN FETCH p.supplier sup WHERE prod.id = :productId")
    List<Pricing> findAllByProductId(@Param("productId") Long productId);

    @Query("SELECT p FROM Pricing p JOIN FETCH p.product prod JOIN FETCH p.supplier sup WHERE sup.id = :supplierId")
    List<Pricing> findAllBySupplierId(@Param("supplierId") Long supplierId);
}

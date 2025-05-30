package com.darkhex.xeroims.repository;

import com.darkhex.xeroims.model.Supplier;
import com.darkhex.xeroims.model.User;
import com.darkhex.xeroims.model.OauthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    // Find all suppliers belonging to a User or OAuthUser, ordered by name with eager loading of category
    @Query("SELECT s FROM Supplier s JOIN FETCH s.category WHERE s.user = :user ORDER BY s.name ASC")
    List<Supplier> findAllByUserOrderByNameAsc(@Param("user") User user);

    @Query("SELECT s FROM Supplier s JOIN FETCH s.category WHERE s.oauthUser = :oauthUser ORDER BY s.name ASC")
    List<Supplier> findAllByOauthUserOrderByNameAsc(@Param("oauthUser") OauthUser oauthUser);

    // Find supplier by email and owner (User or OAuthUser)
    @Query("SELECT s FROM Supplier s JOIN FETCH s.category WHERE s.email = :email AND s.user = :user")
    Optional<Supplier> findByEmailAndUser(@Param("email") String email, @Param("user") User user);

    @Query("SELECT s FROM Supplier s JOIN FETCH s.category WHERE s.email = :email AND s.oauthUser = :oauthUser")
    Optional<Supplier> findByEmailAndOauthUser(@Param("email") String email, @Param("oauthUser") OauthUser oauthUser);

    // Find supplier by name and owner
    @Query("SELECT s FROM Supplier s JOIN FETCH s.category WHERE s.name = :name AND s.user = :user")
    Optional<Supplier> findByNameAndUser(@Param("name") String name, @Param("user") User user);

    @Query("SELECT s FROM Supplier s JOIN FETCH s.category WHERE s.name = :name AND s.oauthUser = :oauthUser")
    Optional<Supplier> findByNameAndOauthUser(@Param("name") String name, @Param("oauthUser") OauthUser oauthUser);

    // Category suppliers
    @Query("SELECT s FROM Supplier s JOIN FETCH s.category WHERE s.category.id = :categoryId")
    List<Supplier> findAllByCategoryId(@Param("categoryId") Long categoryId);

    // Find supplier by ID with eager loading of category
    @Query("SELECT s FROM Supplier s JOIN FETCH s.category WHERE s.id = :id")
    Optional<Supplier> findByIdWithCategory(@Param("id") Long id);

    // Existence checks scoped by owner
    boolean existsByEmailAndUser(String email, User user);

    boolean existsByEmailAndOauthUser(String email, OauthUser oauthUser);

    boolean existsByNameAndUser(String name, User user);

    boolean existsByNameAndOauthUser(String name, OauthUser oauthUser);
}

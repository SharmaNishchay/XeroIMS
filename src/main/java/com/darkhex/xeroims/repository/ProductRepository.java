package com.darkhex.xeroims.repository;

import com.darkhex.xeroims.model.Product;
import com.darkhex.xeroims.model.User;
import com.darkhex.xeroims.model.OauthUser;
import com.darkhex.xeroims.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByUserOrderByNameAsc(User user);

    List<Product> findAllByOauthUserOrderByNameAsc(OauthUser oauthUser);

    Optional<Product> findBySkuAndUser(String sku, User user);

    Optional<Product> findBySkuAndOauthUser(String sku, OauthUser oauthUser);

    List<Product> findAllByCategory(Category category);
}

package com.darkhex.xeroims.repository;

import com.darkhex.xeroims.model.Category;
import com.darkhex.xeroims.model.OauthUser;
import com.darkhex.xeroims.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByUserOrderByNameAsc(User user);

    List<Category> findAllByOauthUserOrderByNameAsc(OauthUser oauthUser);

    Optional<Category> findByNameAndUser(String name, User user);

    Optional<Category> findByNameAndOauthUser(String name, OauthUser oauthUser);
}


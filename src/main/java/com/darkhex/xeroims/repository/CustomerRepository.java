package com.darkhex.xeroims.repository;

import com.darkhex.xeroims.model.Customer;
import com.darkhex.xeroims.model.OauthUser;
import com.darkhex.xeroims.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findAllByUserOrderByNameAsc(User user);
    List<Customer> findAllByOauthUserOrderByNameAsc(OauthUser oauthUser);
    Optional<Customer> findByEmailAndUser(String email, User user);
    Optional<Customer> findByEmailAndOauthUser(String email, OauthUser oauthUser);
    boolean existsByEmailAndUser(String email, User user);
    boolean existsByEmailAndOauthUser(String email, OauthUser oauthUser);
}

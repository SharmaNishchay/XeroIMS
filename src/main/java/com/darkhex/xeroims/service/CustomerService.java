package com.darkhex.xeroims.service;

import com.darkhex.xeroims.model.OauthUser;
import com.darkhex.xeroims.model.User;
import com.darkhex.xeroims.model.Customer;
import com.darkhex.xeroims.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public List<Customer> getAllCustomers(User user, OauthUser oauthUser) {
        if (user != null) {
            return customerRepository.findAllByUserOrderByNameAsc(user);
        } else if (oauthUser != null) {
            return customerRepository.findAllByOauthUserOrderByNameAsc(oauthUser);
        }
        return List.of();
    }
}

package com.darkhex.xeroims.service;

import com.darkhex.xeroims.model.OauthUser;
import com.darkhex.xeroims.repository.OauthUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OauthUserService {

    @Autowired
    private OauthUserRepository oauthUserRepository;

    public Optional<OauthUser> findByEmail(String email) {
        return oauthUserRepository.findByEmail(email);
    }
}

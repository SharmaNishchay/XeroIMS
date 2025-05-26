package com.darkhex.xeroims.security;

import com.darkhex.xeroims.model.OauthUser;
import com.darkhex.xeroims.repository.OauthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.OptionalInt;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final OauthUserRepository oauthUserRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        if (email == null || name == null) {
            OAuth2Error error = new OAuth2Error("invalid_user_info", "Email or name not found from OAuth2 provider", null);
            throw new OAuth2AuthenticationException(error, error.toString());
        }

        Optional<OauthUser> existingUser = oauthUserRepository.findByEmail(email);

        if (existingUser.isEmpty()) {
            String nextId = generateNextId();
            OauthUser newUser = new OauthUser(nextId, name, email);
            oauthUserRepository.save(newUser);
            System.out.println("New OAuth user saved: " + name + " | " + email);
        } else {
            System.out.println("OAuth user already exists: " + email);
        }

        return oauth2User;
    }

    private String generateNextId() {
        OptionalInt maxId = oauthUserRepository.findAll().stream()
                .map(OauthUser::getId)
                .map(id -> id.substring(1)) // Remove 'O'
                .mapToInt(numStr -> {
                    try {
                        return Integer.parseInt(numStr);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max();

        return "O" + (maxId.orElse(0) + 1);
    }
}

package com.darkhex.xeroims.controller;

import com.darkhex.xeroims.model.OauthUser;
import com.darkhex.xeroims.model.User;
import com.darkhex.xeroims.repository.OauthUserRepository;
import com.darkhex.xeroims.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.Optional;

@Controller
public class DashboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OauthUserRepository oauthUserRepository;

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        String name = "User";
        String email = "Not provided";

        if (principal instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2User oAuth2User = oauthToken.getPrincipal();
            name = oAuth2User.getAttribute("name");
            email = oAuth2User.getAttribute("email");
        } else if (principal != null) {
            email = principal.getName();
            name = email;
        }

        model.addAttribute("name", name);
        model.addAttribute("email", email);

        return "dashboard";
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        String email;

        if (principal instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2User oAuth2User = oauthToken.getPrincipal();
            email = oAuth2User.getAttribute("email");
        } else if (principal != null) {
            email = principal.getName();
        } else {
            email = null;
        }

        System.out.println("Profile email: " + email);

        if (email != null) {
            userRepository.findByEmail(email).ifPresentOrElse(user -> {
                System.out.println("Found user in users table: " + user.getName());
                model.addAttribute("id", user.getId());
                model.addAttribute("name", user.getName());
                model.addAttribute("email", user.getEmail());
            }, () -> {
                oauthUserRepository.findByEmail(email).ifPresentOrElse(oauthUser -> {
                    System.out.println("Found user in oauth_users table: " + oauthUser.getName());
                    model.addAttribute("id", oauthUser.getId());
                    model.addAttribute("name", oauthUser.getName());
                    model.addAttribute("email", oauthUser.getEmail());
                }, () -> {
                    System.out.println("No user found with email: " + email);
                });
            });
        }

        return "profile";
    }
}

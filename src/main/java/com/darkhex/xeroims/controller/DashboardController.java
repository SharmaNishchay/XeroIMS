package com.darkhex.xeroims.controller;

import com.darkhex.xeroims.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class DashboardController {

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

        if (principal instanceof OAuth2User oAuth2User) {
            name = oAuth2User.getAttribute("name");
            email = oAuth2User.getAttribute("email");
        } else if (principal != null) {
            name = principal.getName();
            email = name;
        }

        model.addAttribute("name", name);
        model.addAttribute("email", email);

        return "dashboard";
    }


    @Autowired
    private UserRepository userRepository;

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        String email = null;
        if (principal instanceof OAuth2User oAuth2User) {
            email = oAuth2User.getAttribute("email");
        } else if (principal != null) {
            email = principal.getName();
        }
        if (email != null) {
            userRepository.findByEmail(email).ifPresent(user -> {
                model.addAttribute("id", user.getId());
                model.addAttribute("name", user.getName());
                model.addAttribute("email", user.getEmail());
            });
        }
        return "profile";
    }
}

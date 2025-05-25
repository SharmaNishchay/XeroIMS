package com.darkhex.xeroims.controller;

import com.darkhex.xeroims.dto.UserDTO;
import com.darkhex.xeroims.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.ui.Model;

@Controller
@RequiredArgsConstructor
public class RegisterController {
    private final UserService userService;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new UserDTO());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("user") UserDTO userDTO, Model model) {
        boolean success = userService.registerUser(userDTO);
        if (success) {
            return "redirect:/login";
        } else {
            model.addAttribute("error", "Email already exists. Please use a different email.");
            return "register";
        }
    }
}

package com.darkhex.xeroims.controller;

import com.darkhex.xeroims.model.Category;
import com.darkhex.xeroims.model.OauthUser;
import com.darkhex.xeroims.model.User;
import com.darkhex.xeroims.service.CategoryService;
import com.darkhex.xeroims.service.OauthUserService;
import com.darkhex.xeroims.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final UserService userService;
    private final OauthUserService oauthUserService;

    @GetMapping
    public String listCategories(Model model, Authentication authentication) {
        User user = null;
        OauthUser oauthUser = null;

        if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            user = userService.findByEmail(userDetails.getUsername()).orElse(null);
        } else if (authentication.getPrincipal() instanceof OAuth2User oauthUserDetails) {
            String email = oauthUserDetails.getAttribute("email");
            oauthUser = oauthUserService.findByEmail(email).orElse(null);
        }

        model.addAttribute("categories", categoryService.getAllCategories(user, oauthUser));
        return "category";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("category", new Category());
        return "category/add";
    }

    @PostMapping("/add")
    public String addCategory(
            @ModelAttribute @Valid Category category,
            BindingResult result,
            Authentication authentication
    ) {
        if (result.hasErrors()) {
            return "category/add";
        }

        User user = null;
        OauthUser oauthUser = null;

        if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            user = userService.findByEmail(userDetails.getUsername()).orElse(null);
        } else if (authentication.getPrincipal() instanceof OAuth2User oauthUserDetails) {
            String email = oauthUserDetails.getAttribute("email");
            oauthUser = oauthUserService.findByEmail(email).orElse(null);
        }

        categoryService.addCategory(category, user, oauthUser);
        return "redirect:/category";
    }

    @GetMapping("/edit/{id}")
    public String editCategory(@PathVariable Long id, Model model) {
        model.addAttribute("category", categoryService.getCategoryById(id));
        return "category/edit";
    }

    @PostMapping("/update/{id}")
    public String updateCategory(@PathVariable Long id,
                                 @ModelAttribute @Valid Category category,
                                 BindingResult result) {
        if (result.hasErrors()) {
            return "category/edit";
        }
        categoryService.updateCategory(id, category);
        return "redirect:/category";
    }

    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return "redirect:/category";
    }
    
}

package com.darkhex.xeroims.controller;

import com.darkhex.xeroims.dto.ProductDTO;
import com.darkhex.xeroims.model.OauthUser;
import com.darkhex.xeroims.model.Product;
import com.darkhex.xeroims.model.User;
import com.darkhex.xeroims.service.CategoryService;
import com.darkhex.xeroims.service.OauthUserService;
import com.darkhex.xeroims.service.ProductService;
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
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final UserService userService;
    private final OauthUserService oauthUserService;
    private final CategoryService categoryService;

    private User getUser(Authentication authentication) {
        if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            return userService.findByEmail(userDetails.getUsername()).orElse(null);
        }
        return null;
    }

    private OauthUser getOauthUser(Authentication authentication) {
        if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            return oauthUserService.findByEmail(email).orElse(null);
        }
        return null;
    }

    @GetMapping
    public String listProducts(Model model, Authentication authentication) {
        User user = getUser(authentication);
        OauthUser oauthUser = getOauthUser(authentication);

        model.addAttribute("products", productService.getAllProducts(user, oauthUser));
        return "products";
    }

    @GetMapping("/add")
    public String showAddForm(Model model, Authentication authentication) {
        model.addAttribute("productDTO", new ProductDTO());

        User user = getUser(authentication);
        OauthUser oauthUser = getOauthUser(authentication);

        model.addAttribute("categories", categoryService.getAllCategories(user, oauthUser));
        return "products/add";
    }

    @PostMapping("/add")
    public String addProduct(
            @ModelAttribute("productDTO") @Valid ProductDTO productDTO,
            BindingResult result,
            Authentication authentication,
            Model model
    ) {
        if (result.hasErrors()) {
            return "products/add";
        }

        User user = getUser(authentication);
        OauthUser oauthUser = getOauthUser(authentication);

        try {
            productService.addProduct(productDTO, user, oauthUser);
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "products/add";
        }

        return "redirect:/products";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, Authentication authentication) {
        Product product = productService.getProductById(id);

        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setSku(product.getSku());
        dto.setPrice(product.getPrice());
        dto.setQuantity(product.getQuantity());
        dto.setCategoryId(product.getCategory().getId());

        User user = getUser(authentication);
        OauthUser oauthUser = getOauthUser(authentication);

        model.addAttribute("categories", categoryService.getAllCategories(user, oauthUser));
        model.addAttribute("productDTO", dto);
        return "products/edit";
    }

    @PostMapping("/update/{id}")
    public String updateProduct(
            @PathVariable Long id,
            @ModelAttribute("productDTO") @Valid ProductDTO productDTO,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) {
            return "products/edit";
        }

        try {
            productService.updateProduct(id, productDTO);
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "products/edit";
        }

        return "redirect:/products";
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/products";
    }
}
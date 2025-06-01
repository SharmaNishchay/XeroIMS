package com.darkhex.xeroims.controller;

import com.darkhex.xeroims.dto.PricingDTO;
import com.darkhex.xeroims.model.OauthUser;
import com.darkhex.xeroims.model.Pricing;
import com.darkhex.xeroims.model.User;
import com.darkhex.xeroims.service.CategoryService;
import com.darkhex.xeroims.service.OauthUserService;
import com.darkhex.xeroims.service.PricingService;
import com.darkhex.xeroims.service.ProductService;
import com.darkhex.xeroims.service.SupplierService;
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

import java.util.Collections;

@Controller
@RequestMapping("/pricing")
@RequiredArgsConstructor
public class PricingController {

    private final PricingService pricingService;
    private final UserService userService;
    private final OauthUserService oauthUserService;
    private final CategoryService categoryService;
    private final SupplierService supplierService;
    private final ProductService productService;

    private User getUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            return userService.findByEmail(userDetails.getUsername()).orElse(null);
        }
        return null;
    }

    private OauthUser getOauthUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            return oauthUserService.findByEmail(email).orElse(null);
        }
        return null;
    }

    @GetMapping
    public String listPricing(Model model, Authentication authentication) {
        User user = getUser(authentication);
        OauthUser oauthUser = getOauthUser(authentication);

        // Fixed: Changed from "pricing" to "pricings" to match the HTML template
        model.addAttribute("pricings", pricingService.getAllPricings(user, oauthUser));
        return "pricing";
    }

    @GetMapping("/add")
    public String showAddForm(Model model, Authentication authentication) {
        model.addAttribute("pricingDTO", new PricingDTO());
        model.addAttribute("selectedCategoryId", null);

        User user = getUser(authentication);
        OauthUser oauthUser = getOauthUser(authentication);

        try {
            model.addAttribute("categories", categoryService.getAllCategories(user, oauthUser));
        } catch (Exception e) {
            System.err.println("Error fetching categories: " + e.getMessage());
            model.addAttribute("categories", Collections.emptyList());
        }

        try {
            model.addAttribute("suppliers", supplierService.getAllSuppliers(user, oauthUser));
        } catch (Exception e) {
            System.err.println("Error fetching suppliers: " + e.getMessage());
            model.addAttribute("suppliers", Collections.emptyList());
        }

        try {
            model.addAttribute("products", productService.getAllProducts(user, oauthUser));
        } catch (Exception e) {
            System.err.println("Error fetching products: " + e.getMessage());
            model.addAttribute("products", Collections.emptyList());
        }

        return "pricing/add";
    }

    @PostMapping("/loadProducts")
    public String loadProducts(@RequestParam(value = "selectedCategoryId", required = false) Long selectedCategoryId,
                               @ModelAttribute("pricingDTO") PricingDTO pricingDTO,
                               Model model,
                               Authentication authentication) {
        User user = getUser(authentication);
        OauthUser oauthUser = getOauthUser(authentication);

        model.addAttribute("categories", categoryService.getAllCategories(user, oauthUser));
        model.addAttribute("suppliers", supplierService.getAllSuppliers(user, oauthUser));
        model.addAttribute("products", selectedCategoryId != null
                ? productService.getAllProducts(user, oauthUser) // Temporary fallback
                : productService.getAllProducts(user, oauthUser));
        model.addAttribute("selectedCategoryId", selectedCategoryId);
        model.addAttribute("pricingDTO", pricingDTO);

        return "pricing/add";
    }

    @PostMapping("/add")
    public String addPricing(
            @ModelAttribute("pricingDTO") @Valid PricingDTO pricingDTO,
            BindingResult result,
            @RequestParam(value = "selectedCategoryId", required = false) Long selectedCategoryId,
            Model model,
            Authentication authentication
    ) {
        User user = getUser(authentication);
        OauthUser oauthUser = getOauthUser(authentication);

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories(user, oauthUser));
            model.addAttribute("suppliers", supplierService.getAllSuppliers(user, oauthUser));
            model.addAttribute("products", selectedCategoryId != null
                    ? productService.getAllProducts(user, oauthUser) // Temporary fallback
                    : productService.getAllProducts(user, oauthUser));
            model.addAttribute("selectedCategoryId", selectedCategoryId);
            return "pricing/add";
        }

        try {
            pricingService.addPricing(pricingDTO, user, oauthUser);
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("categories", categoryService.getAllCategories(user, oauthUser));
            model.addAttribute("suppliers", supplierService.getAllSuppliers(user, oauthUser));
            model.addAttribute("products", selectedCategoryId != null
                    ? productService.getAllProducts(user, oauthUser) // Temporary fallback
                    : productService.getAllProducts(user, oauthUser));
            model.addAttribute("selectedCategoryId", selectedCategoryId);
            return "pricing/add";
        }

        return "redirect:/pricing";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, Authentication authentication) {
        Pricing pricing = pricingService.getPricingById(id);

        PricingDTO dto = new PricingDTO();
        dto.setId(pricing.getId());
        dto.setPrice(pricing.getPrice());
        dto.setProductId(pricing.getProduct().getId());
        dto.setSupplierId(pricing.getSupplier().getId());

        User user = getUser(authentication);
        OauthUser oauthUser = getOauthUser(authentication);

        Long selectedCategoryId = pricing.getProduct().getCategory() != null
                ? pricing.getProduct().getCategory().getId()
                : null;

        model.addAttribute("categories", categoryService.getAllCategories(user, oauthUser));
        model.addAttribute("suppliers", supplierService.getAllSuppliers(user, oauthUser));
        model.addAttribute("products", selectedCategoryId != null
                ? productService.getAllProducts(user, oauthUser) // Temporary fallback
                : productService.getAllProducts(user, oauthUser));
        model.addAttribute("selectedCategoryId", selectedCategoryId);
        model.addAttribute("pricingDTO", dto);

        return "pricing/edit";
    }

    @PostMapping("/update/{id}")
    public String updatePricing(
            @PathVariable Long id,
            @ModelAttribute("pricingDTO") @Valid PricingDTO pricingDTO,
            BindingResult result,
            @RequestParam(value = "selectedCategoryId", required = false) Long selectedCategoryId,
            Model model,
            Authentication authentication
    ) {
        User user = getUser(authentication);
        OauthUser oauthUser = getOauthUser(authentication);

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories(user, oauthUser));
            model.addAttribute("suppliers", supplierService.getAllSuppliers(user, oauthUser));
            model.addAttribute("products", selectedCategoryId != null
                    ? productService.getAllProducts(user, oauthUser) // Temporary fallback
                    : productService.getAllProducts(user, oauthUser));
            model.addAttribute("selectedCategoryId", selectedCategoryId);
            return "pricing/edit";
        }

        try {
            pricingService.updatePricing(id, pricingDTO);
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("categories", categoryService.getAllCategories(user, oauthUser));
            model.addAttribute("suppliers", supplierService.getAllSuppliers(user, oauthUser));
            model.addAttribute("products", selectedCategoryId != null
                    ? productService.getAllProducts(user, oauthUser) // Temporary fallback
                    : productService.getAllProducts(user, oauthUser));
            model.addAttribute("selectedCategoryId", selectedCategoryId);
            return "pricing/edit";
        }

        return "redirect:/pricing";
    }

    @GetMapping("/delete/{id}")
    public String deletePricing(@PathVariable Long id) {
        pricingService.deletePricing(id);
        return "redirect:/pricing";
    }
}
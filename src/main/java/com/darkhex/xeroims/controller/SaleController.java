package com.darkhex.xeroims.controller;

import com.darkhex.xeroims.dto.SaleDTO;
import com.darkhex.xeroims.enums.Status;
import com.darkhex.xeroims.exception.ResourceNotFoundException;
import com.darkhex.xeroims.model.OauthUser;
import com.darkhex.xeroims.model.Product;
import com.darkhex.xeroims.model.User;
import com.darkhex.xeroims.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

@Controller
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;
    private final UserService userService;
    private final OauthUserService oauthUserService;
    private final CategoryService categoryService;
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
    public String showSalesForm(Model model, Authentication authentication,
                               @ModelAttribute("successMessage") String successMessage) {
        SaleDTO saleDTO = new SaleDTO();
        saleDTO.setSalesDate(LocalDate.now());
        saleDTO.setStatus(Status.PENDING);

        User user = getUser(authentication);
        OauthUser oauthUser = getOauthUser(authentication);

        try {
            model.addAttribute("categories", categoryService.getAllCategories(user, oauthUser));
            model.addAttribute("products", Collections.emptyList()); // Will be populated when category is selected
        } catch (Exception e) {
            model.addAttribute("categories", Collections.emptyList());
            model.addAttribute("products", Collections.emptyList());
        }

        // Pass the success message if it exists
        if (successMessage != null && !successMessage.isEmpty()) {
            model.addAttribute("successMessage", successMessage);
        }

        model.addAttribute("salesDTO", saleDTO);
        return "sales";
    }

    @PostMapping("/loadProducts")
    public String loadProducts(@RequestParam(value = "categoryId", required = false) Long categoryId,
                              @ModelAttribute("salesDTO") SaleDTO saleDTO,
                              Model model,
                              Authentication authentication) {
        User user = getUser(authentication);
        OauthUser oauthUser = getOauthUser(authentication);

        model.addAttribute("categories", categoryService.getAllCategories(user, oauthUser));

        if (categoryId != null) {
            // Filter products by the selected category
            model.addAttribute("products", productService.getProductsByCategory(categoryId, user, oauthUser));
        } else {
            model.addAttribute("products", productService.getAllProducts(user, oauthUser));
        }

        saleDTO.setCategoryId(categoryId);
        // Reset product selection and price when category changes
        saleDTO.setProductId(null);
        saleDTO.setPrice(null);
        saleDTO.setTotalPrice(null);

        model.addAttribute("salesDTO", saleDTO);
        return "sales";
    }

    @PostMapping("/loadProductPrice")
    public String loadProductPrice(@RequestParam(value = "categoryId", required = false) Long categoryId,
                                  @RequestParam(value = "productId", required = false) Long productId,
                                  @ModelAttribute("salesDTO") SaleDTO saleDTO,
                                  Model model,
                                  Authentication authentication) {
        User user = getUser(authentication);
        OauthUser oauthUser = getOauthUser(authentication);

        model.addAttribute("categories", categoryService.getAllCategories(user, oauthUser));

        if (categoryId != null) {
            model.addAttribute("products", productService.getProductsByCategory(categoryId, user, oauthUser));
        } else {
            model.addAttribute("products", productService.getAllProducts(user, oauthUser));
        }

        saleDTO.setCategoryId(categoryId);
        saleDTO.setProductId(productId);

        // Automatically fetch price when product is selected
        if (productId != null) {
            Optional<Product> product = Optional.ofNullable(productService.getProductById(productId));
            if (product.isPresent()) {
                saleDTO.setPrice(product.get().getPrice());

                // Calculate total price if quantity is available
                if (saleDTO.getQuantity() != null && saleDTO.getQuantity() > 0) {
                    BigDecimal totalPrice = product.get().getPrice()
                            .multiply(new BigDecimal(saleDTO.getQuantity()));
                    saleDTO.setTotalPrice(totalPrice);
                }
            }
        } else {
            // Reset price and total price if product is not selected
            saleDTO.setPrice(null);
            saleDTO.setTotalPrice(null);
        }

        model.addAttribute("salesDTO", saleDTO);
        return "sales";
    }

    @PostMapping("/calculateTotal")
    public String calculateTotal(@ModelAttribute("salesDTO") SaleDTO saleDTO,
                               Model model,
                               Authentication authentication) {
        User user = getUser(authentication);
        OauthUser oauthUser = getOauthUser(authentication);

        // Calculate total price if price and quantity are available
        if (saleDTO.getPrice() != null && saleDTO.getQuantity() != null && saleDTO.getQuantity() > 0) {
            BigDecimal totalPrice = saleDTO.getPrice().multiply(new BigDecimal(saleDTO.getQuantity()));
            saleDTO.setTotalPrice(totalPrice);
        }

        // Reload all form data
        model.addAttribute("categories", categoryService.getAllCategories(user, oauthUser));

        if (saleDTO.getCategoryId() != null) {
            model.addAttribute("products", productService.getProductsByCategory(saleDTO.getCategoryId(), user, oauthUser));
        } else {
            model.addAttribute("products", productService.getAllProducts(user, oauthUser));
        }

        model.addAttribute("salesDTO", saleDTO);
        return "sales";
    }

    @PostMapping("/save")
    public String saveSale(
            @ModelAttribute("salesDTO") @Valid SaleDTO saleDTO,
            BindingResult result,
            Model model,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        User user = getUser(authentication);
        OauthUser oauthUser = getOauthUser(authentication);

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories(user, oauthUser));

            if (saleDTO.getCategoryId() != null) {
                model.addAttribute("products", productService.getProductsByCategory(saleDTO.getCategoryId(), user, oauthUser));
            } else {
                model.addAttribute("products", productService.getAllProducts(user, oauthUser));
            }

            return "sales";
        }

        try {
            // Fetch price if not already set
            if (saleDTO.getPrice() == null) {
                Optional<Product> product = Optional.ofNullable(productService.getProductById(saleDTO.getProductId()));
                if (product.isPresent()) {
                    saleDTO.setPrice(product.get().getPrice());
                    BigDecimal totalPrice = product.get().getPrice().multiply(new BigDecimal(saleDTO.getQuantity()));
                    saleDTO.setTotalPrice(totalPrice);
                } else {
                    throw new ResourceNotFoundException("Product not found");
                }
            }

            saleService.createSale(saleDTO, user, oauthUser);
            redirectAttributes.addFlashAttribute("successMessage", "Sale recorded successfully!");
            return "redirect:/sales";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("categories", categoryService.getAllCategories(user, oauthUser));

            if (saleDTO.getCategoryId() != null) {
                model.addAttribute("products", productService.getProductsByCategory(saleDTO.getCategoryId(), user, oauthUser));
            } else {
                model.addAttribute("products", productService.getAllProducts(user, oauthUser));
            }

            return "sales";
        }
    }

    @GetMapping("/list")
    public String listSales(Model model, Authentication authentication) {
        User user = getUser(authentication);
        OauthUser oauthUser = getOauthUser(authentication);

        model.addAttribute("sales", saleService.getAllSales(user, oauthUser));
        return "salesList";
    }
}

package com.darkhex.xeroims.controller;

import com.darkhex.xeroims.dto.PurchaseDTO;
import com.darkhex.xeroims.enums.Status;
import com.darkhex.xeroims.exception.ResourceNotFoundException;
import com.darkhex.xeroims.model.OauthUser;
import com.darkhex.xeroims.model.Pricing;
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
@RequestMapping("/purchase")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final UserService userService;
    private final OauthUserService oauthUserService;
    private final CategoryService categoryService;
    private final SupplierService supplierService;
    private final ProductService productService;
    private final PricingService pricingService;

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
    public String showPurchaseForm(Model model, Authentication authentication,
                                  @ModelAttribute("successMessage") String successMessage) {
        PurchaseDTO purchaseDTO = new PurchaseDTO();
        purchaseDTO.setPurchaseDate(LocalDate.now());
        purchaseDTO.setStatus(Status.PENDING);

        User user = getUser(authentication);
        OauthUser oauthUser = getOauthUser(authentication);

        try {
            model.addAttribute("categories", categoryService.getAllCategories(user, oauthUser));
            model.addAttribute("suppliers", supplierService.getAllSuppliers(user, oauthUser));
            model.addAttribute("products", productService.getAllProducts(user, oauthUser));
        } catch (Exception e) {
            model.addAttribute("categories", Collections.emptyList());
            model.addAttribute("suppliers", Collections.emptyList());
            model.addAttribute("products", Collections.emptyList());
        }

        // Pass the success message if it exists
        if (successMessage != null && !successMessage.isEmpty()) {
            model.addAttribute("successMessage", successMessage);
        }

        model.addAttribute("purchaseDTO", purchaseDTO);
        return "purchase";
    }

    @PostMapping("/loadProducts")
    public String loadProducts(@RequestParam(value = "categoryId", required = false) Long categoryId,
                              @ModelAttribute("purchaseDTO") PurchaseDTO purchaseDTO,
                              Model model,
                              Authentication authentication) {
        User user = getUser(authentication);
        OauthUser oauthUser = getOauthUser(authentication);

        model.addAttribute("categories", categoryService.getAllCategories(user, oauthUser));

        if (categoryId != null) {
            // Filter both products and suppliers by the selected category
            model.addAttribute("products", productService.getProductsByCategory(categoryId, user, oauthUser));
            model.addAttribute("suppliers", supplierService.getSuppliersByCategory(categoryId, user, oauthUser));
        } else {
            model.addAttribute("products", productService.getAllProducts(user, oauthUser));
            model.addAttribute("suppliers", supplierService.getAllSuppliers(user, oauthUser));
        }

        purchaseDTO.setCategoryId(categoryId);
        // Reset product and supplier selection when category changes
        purchaseDTO.setProductId(null);
        purchaseDTO.setSupplierId(null);
        purchaseDTO.setPrice(null);
        purchaseDTO.setTotalPrice(null);

        model.addAttribute("purchaseDTO", purchaseDTO);
        return "purchase";
    }

    @PostMapping("/loadSuppliers")
    public String loadSuppliers(@RequestParam(value = "categoryId", required = false) Long categoryId,
                               @RequestParam(value = "productId", required = false) Long productId,
                               @RequestParam(value = "supplierId", required = false) Long supplierId,
                               @ModelAttribute("purchaseDTO") PurchaseDTO purchaseDTO,
                               Model model,
                               Authentication authentication) {
        User user = getUser(authentication);
        OauthUser oauthUser = getOauthUser(authentication);

        model.addAttribute("categories", categoryService.getAllCategories(user, oauthUser));

        if (categoryId != null) {
            model.addAttribute("products", productService.getProductsByCategory(categoryId, user, oauthUser));
            model.addAttribute("suppliers", supplierService.getSuppliersByCategory(categoryId, user, oauthUser));
        } else {
            model.addAttribute("products", productService.getAllProducts(user, oauthUser));
            model.addAttribute("suppliers", supplierService.getAllSuppliers(user, oauthUser));
        }

        purchaseDTO.setCategoryId(categoryId);
        purchaseDTO.setProductId(productId);
        purchaseDTO.setSupplierId(supplierId);

        // Automatically fetch price when both product and supplier are selected
        if (productId != null && supplierId != null) {
            Optional<Pricing> pricing = pricingService.findByProductAndSupplier(
                    productId, supplierId, user, oauthUser);

            if (pricing.isPresent()) {
                purchaseDTO.setPrice(pricing.get().getPrice());

                // Calculate total price if quantity is available
                if (purchaseDTO.getQuantity() != null && purchaseDTO.getQuantity() > 0) {
                    BigDecimal totalPrice = pricing.get().getPrice()
                            .multiply(new BigDecimal(purchaseDTO.getQuantity()));
                    purchaseDTO.setTotalPrice(totalPrice);
                }
            }
        } else {
            // Reset price and total price if either product or supplier is not selected
            purchaseDTO.setPrice(null);
            purchaseDTO.setTotalPrice(null);
        }

        model.addAttribute("purchaseDTO", purchaseDTO);
        return "purchase";
    }

    @PostMapping("/fetchPrice")
    public String fetchPrice(@ModelAttribute("purchaseDTO") PurchaseDTO purchaseDTO,
                            Model model,
                            Authentication authentication) {
        User user = getUser(authentication);
        OauthUser oauthUser = getOauthUser(authentication);

        // Only fetch price if both product and supplier are selected
        if (purchaseDTO.getProductId() != null && purchaseDTO.getSupplierId() != null) {
            Optional<Pricing> pricing = pricingService.findByProductAndSupplier(
                    purchaseDTO.getProductId(),
                    purchaseDTO.getSupplierId(),
                    user,
                    oauthUser);

            if (pricing.isPresent()) {
                purchaseDTO.setPrice(pricing.get().getPrice());

                // Calculate total price if quantity is available
                if (purchaseDTO.getQuantity() != null && purchaseDTO.getQuantity() > 0) {
                    BigDecimal totalPrice = pricing.get().getPrice()
                            .multiply(new BigDecimal(purchaseDTO.getQuantity()));
                    purchaseDTO.setTotalPrice(totalPrice);
                }
            }
        }

        // Reload all form data
        model.addAttribute("categories", categoryService.getAllCategories(user, oauthUser));

        if (purchaseDTO.getCategoryId() != null) {
            model.addAttribute("products", productService.getProductsByCategory(purchaseDTO.getCategoryId(), user, oauthUser));
            model.addAttribute("suppliers", supplierService.getSuppliersByCategory(purchaseDTO.getCategoryId(), user, oauthUser));
        } else {
            model.addAttribute("products", productService.getAllProducts(user, oauthUser));
            model.addAttribute("suppliers", supplierService.getAllSuppliers(user, oauthUser));
        }

        model.addAttribute("purchaseDTO", purchaseDTO);
        return "purchase";
    }

    @PostMapping("/calculateTotal")
    public String calculateTotal(@ModelAttribute("purchaseDTO") PurchaseDTO purchaseDTO,
                               Model model,
                               Authentication authentication) {
        User user = getUser(authentication);
        OauthUser oauthUser = getOauthUser(authentication);

        // Calculate total price if price and quantity are available
        if (purchaseDTO.getPrice() != null && purchaseDTO.getQuantity() != null && purchaseDTO.getQuantity() > 0) {
            BigDecimal totalPrice = purchaseDTO.getPrice().multiply(new BigDecimal(purchaseDTO.getQuantity()));
            purchaseDTO.setTotalPrice(totalPrice);
        }

        // Reload all form data
        model.addAttribute("categories", categoryService.getAllCategories(user, oauthUser));

        if (purchaseDTO.getCategoryId() != null) {
            model.addAttribute("products", productService.getProductsByCategory(purchaseDTO.getCategoryId(), user, oauthUser));
            model.addAttribute("suppliers", supplierService.getSuppliersByCategory(purchaseDTO.getCategoryId(), user, oauthUser));
        } else {
            model.addAttribute("products", productService.getAllProducts(user, oauthUser));
            model.addAttribute("suppliers", supplierService.getAllSuppliers(user, oauthUser));
        }

        model.addAttribute("purchaseDTO", purchaseDTO);
        return "purchase";
    }

    @PostMapping("/save")
    public String savePurchase(
            @ModelAttribute("purchaseDTO") @Valid PurchaseDTO purchaseDTO,
            BindingResult result,
            Model model,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        User user = getUser(authentication);
        OauthUser oauthUser = getOauthUser(authentication);

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories(user, oauthUser));

            if (purchaseDTO.getCategoryId() != null) {
                model.addAttribute("products", productService.getProductsByCategory(purchaseDTO.getCategoryId(), user, oauthUser));
                model.addAttribute("suppliers", supplierService.getSuppliersByCategory(purchaseDTO.getCategoryId(), user, oauthUser));
            } else {
                model.addAttribute("products", productService.getAllProducts(user, oauthUser));
                model.addAttribute("suppliers", supplierService.getAllSuppliers(user, oauthUser));
            }

            return "purchase";
        }

        try {
            // Fetch price if not already set
            if (purchaseDTO.getPrice() == null) {
                Optional<Pricing> pricing = pricingService.findByProductAndSupplier(
                        purchaseDTO.getProductId(), purchaseDTO.getSupplierId(), user, oauthUser);

                if (pricing.isPresent()) {
                    purchaseDTO.setPrice(pricing.get().getPrice());
                    BigDecimal totalPrice = pricing.get().getPrice().multiply(new BigDecimal(purchaseDTO.getQuantity()));
                    purchaseDTO.setTotalPrice(totalPrice);
                } else {
                    throw new ResourceNotFoundException("No pricing found for this product-supplier combination");
                }
            }

            purchaseService.createPurchase(purchaseDTO, user, oauthUser);
            redirectAttributes.addFlashAttribute("successMessage", "Purchase created successfully! Your order will be processed shortly.");
            return "redirect:/purchase";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("categories", categoryService.getAllCategories(user, oauthUser));

            if (purchaseDTO.getCategoryId() != null) {
                model.addAttribute("products", productService.getProductsByCategory(purchaseDTO.getCategoryId(), user, oauthUser));
                model.addAttribute("suppliers", supplierService.getSuppliersByCategory(purchaseDTO.getCategoryId(), user, oauthUser));
            } else {
                model.addAttribute("products", productService.getAllProducts(user, oauthUser));
                model.addAttribute("suppliers", supplierService.getAllSuppliers(user, oauthUser));
            }

            return "purchase";
        }
    }

    @GetMapping("/list")
    public String listPurchases(Model model, Authentication authentication) {
        User user = getUser(authentication);
        OauthUser oauthUser = getOauthUser(authentication);

        model.addAttribute("purchases", purchaseService.getAllPurchases(user, oauthUser));
        return "purchases";
    }
}

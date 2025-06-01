package com.darkhex.xeroims.controller;

import com.darkhex.xeroims.dto.SupplierDTO;
import com.darkhex.xeroims.model.OauthUser;
import com.darkhex.xeroims.model.Supplier;
import com.darkhex.xeroims.model.User;
import com.darkhex.xeroims.service.CategoryService;
import com.darkhex.xeroims.service.OauthUserService;
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

@Controller
@RequestMapping("/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;
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
    public String listSuppliers(Model model, Authentication authentication) {
        User user = getUser(authentication);
        OauthUser oauthUser = getOauthUser(authentication);

        model.addAttribute("suppliers", supplierService.getAllSuppliers(user, oauthUser));
        return "suppliers";
    }

    @GetMapping("/add")
    public String showAddForm(Model model, Authentication authentication) {
        model.addAttribute("supplierDTO", new SupplierDTO());

        User user = getUser(authentication);
        OauthUser oauthUser = getOauthUser(authentication);

        model.addAttribute("categories", categoryService.getAllCategories(user, oauthUser));
        return "suppliers/add";
    }

    @PostMapping("/add")
    public String addSupplier(
            @ModelAttribute("supplierDTO") @Valid SupplierDTO supplierDTO,
            BindingResult result,
            Authentication authentication,
            Model model
    ) {
        if (result.hasErrors()) {
            User user = getUser(authentication);
            OauthUser oauthUser = getOauthUser(authentication);
            model.addAttribute("categories", categoryService.getAllCategories(user, oauthUser));

            return "suppliers/add";
        }

        User user = getUser(authentication);
        OauthUser oauthUser = getOauthUser(authentication);

        try {
            supplierService.addSupplier(supplierDTO, user, oauthUser);
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("categories", categoryService.getAllCategories(user, oauthUser));
            return "suppliers/add";
        }

        return "redirect:/suppliers";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, Authentication authentication) {
        Supplier supplier = supplierService.getSupplierById(id);

        SupplierDTO dto = new SupplierDTO();
        dto.setId(supplier.getId());
        dto.setName(supplier.getName());
        dto.setPhone(supplier.getPhone());
        dto.setEmail(supplier.getEmail());
        dto.setAddress(supplier.getAddress());

        if (supplier.getCategory() != null) {
            dto.setCategoryId(supplier.getCategory().getId());
        }

        model.addAttribute("supplierDTO", dto);

        User user = getUser(authentication);
        OauthUser oauthUser = getOauthUser(authentication);

        model.addAttribute("categories", categoryService.getAllCategories(user, oauthUser));

        return "suppliers/edit";
    }

    @PostMapping("/update/{id}")
    public String updateSupplier(
            @PathVariable Long id,
            @ModelAttribute("supplierDTO") @Valid SupplierDTO supplierDTO,
            BindingResult result,
            Authentication authentication,
            Model model
    ) {
        if (result.hasErrors()) {
            User user = getUser(authentication);
            OauthUser oauthUser = getOauthUser(authentication);
            model.addAttribute("categories", categoryService.getAllCategories(user, oauthUser));

            return "suppliers/edit";
        }

        try {
            supplierService.updateSupplier(id, supplierDTO);
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());

            User user = getUser(authentication);
            OauthUser oauthUser = getOauthUser(authentication);
            model.addAttribute("categories", categoryService.getAllCategories(user, oauthUser));

            return "suppliers/edit";
        }

        return "redirect:/suppliers";
    }

    @GetMapping("/delete/{id}")
    public String deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return "redirect:/suppliers";
    }
}

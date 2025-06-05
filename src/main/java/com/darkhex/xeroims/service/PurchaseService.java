package com.darkhex.xeroims.service;

import com.darkhex.xeroims.dto.PurchaseDTO;
import com.darkhex.xeroims.exception.ResourceNotFoundException;
import com.darkhex.xeroims.model.*;
import com.darkhex.xeroims.repository.PricingRepository;
import com.darkhex.xeroims.repository.ProductRepository;
import com.darkhex.xeroims.repository.PurchaseRepository;
import com.darkhex.xeroims.repository.SupplierRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final PricingRepository pricingRepository;
    private final ProductService productService;

    public List<Purchase> getAllPurchases(User user, OauthUser oauthUser) {
        if (user != null) {
            return purchaseRepository.findAllByUserOrderByPurchaseDateDesc(user);
        } else if (oauthUser != null) {
            return purchaseRepository.findAllByOauthUserOrderByPurchaseDateDesc(oauthUser);
        }
        return List.of();
    }

    public Purchase getPurchaseById(Long id) {
        return purchaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found with id: " + id));
    }

    @Transactional
    public Purchase createPurchase(PurchaseDTO dto, User user, OauthUser oauthUser) {
        if (user == null && oauthUser == null) {
            throw new IllegalArgumentException("User context must be provided.");
        }

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + dto.getProductId()));

        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + dto.getSupplierId()));

        Optional<Pricing> pricingOptional;
        if (user != null) {
            pricingOptional = pricingRepository.findByProductIdAndSupplierIdAndUser(
                    product.getId(), supplier.getId(), user);
        } else {
            pricingOptional = pricingRepository.findByProductIdAndSupplierIdAndOauthUser(
                    product.getId(), supplier.getId(), oauthUser);
        }

        Pricing pricing;
        if (pricingOptional.isPresent()) {
            pricing = pricingOptional.get();
        } else {
            pricing = new Pricing();
            pricing.setProduct(product);
            pricing.setSupplier(supplier);
            pricing.setPrice(product.getPrice());
            pricing.setUser(user);
            pricing.setOauthUser(oauthUser);
            pricing = pricingRepository.save(pricing);
        }

        BigDecimal totalPrice;
        if (dto.getTotalPrice() != null) {
            totalPrice = dto.getTotalPrice();
        } else {
            totalPrice = pricing.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity()));
        }

        LocalDate purchaseDate = dto.getPurchaseDate();
        if (purchaseDate == null) {
            purchaseDate = LocalDate.now();
        }

        Purchase purchase = new Purchase(
                pricing,
                product,
                supplier,
                dto.getQuantity(),
                totalPrice,
                purchaseDate,
                dto.getStatus(),
                user,
                oauthUser
        );

        product.setQuantity(product.getQuantity() + dto.getQuantity());
        productRepository.save(product);

        return purchaseRepository.save(purchase);
    }

    @Transactional
    public void deletePurchase(Long id) {
        Purchase purchase = getPurchaseById(id);
        Product product = purchase.getProduct();
        product.setQuantity(product.getQuantity() - purchase.getQuantity());
        productRepository.save(product);

        purchaseRepository.deleteById(id);
    }
}

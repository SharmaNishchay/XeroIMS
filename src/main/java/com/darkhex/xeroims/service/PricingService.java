package com.darkhex.xeroims.service;

import com.darkhex.xeroims.dto.PricingDTO;
import com.darkhex.xeroims.exception.ResourceNotFoundException;
import com.darkhex.xeroims.model.OauthUser;
import com.darkhex.xeroims.model.Pricing;
import com.darkhex.xeroims.model.Product;
import com.darkhex.xeroims.model.Supplier;
import com.darkhex.xeroims.model.User;
import com.darkhex.xeroims.repository.PricingRepository;
import com.darkhex.xeroims.repository.ProductRepository;
import com.darkhex.xeroims.repository.SupplierRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PricingService {

    private final PricingRepository pricingRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;

    public List<Pricing> getAllPricings(User user, OauthUser oauthUser) {
        if (user != null) {
            return pricingRepository.findAllByUserOrderByPriceAsc(user);
        } else if (oauthUser != null) {
            return pricingRepository.findAllByOauthUserOrderByPriceAsc(oauthUser);
        }
        return List.of();
    }

    public Pricing getPricingById(Long id) {
        return pricingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing not found with id: " + id));
    }

    @Transactional
    public Pricing addPricing(PricingDTO dto, User user, OauthUser oauthUser) {
        if (user == null && oauthUser == null) {
            throw new IllegalArgumentException("User context must be provided.");
        }

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + dto.getProductId()));

        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + dto.getSupplierId()));

        Pricing pricing = new Pricing();
        pricing.setProduct(product);
        pricing.setSupplier(supplier);
        pricing.setPrice(dto.getPrice());
        pricing.setUser(user);
        pricing.setOauthUser(oauthUser);

        return pricingRepository.save(pricing);
    }

    @Transactional
    public Pricing updatePricing(Long id, PricingDTO dto) {
        Pricing existingPricing = getPricingById(id);

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + dto.getProductId()));

        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + dto.getSupplierId()));

        existingPricing.setProduct(product);
        existingPricing.setSupplier(supplier);
        existingPricing.setPrice(dto.getPrice());

        return pricingRepository.save(existingPricing);
    }

    @Transactional
    public void deletePricing(Long id) {
        if (!pricingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Pricing not found with id: " + id);
        }
        pricingRepository.deleteById(id);
    }

    public Optional<Pricing> findByProductAndSupplier(Long productId, Long supplierId, User user, OauthUser oauthUser) {
        if (user != null) {
            return pricingRepository.findByProductIdAndSupplierIdAndUser(productId, supplierId, user);
        } else if (oauthUser != null) {
            return pricingRepository.findByProductIdAndSupplierIdAndOauthUser(productId, supplierId, oauthUser);
        }
        return Optional.empty();
    }
}

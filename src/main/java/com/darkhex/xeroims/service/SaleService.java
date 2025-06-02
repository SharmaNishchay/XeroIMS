package com.darkhex.xeroims.service;

import com.darkhex.xeroims.dto.SaleDTO;
import com.darkhex.xeroims.exception.ResourceNotFoundException;
import com.darkhex.xeroims.model.*;
import com.darkhex.xeroims.repository.PricingRepository;
import com.darkhex.xeroims.repository.ProductRepository;
import com.darkhex.xeroims.repository.SaleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final PricingRepository pricingRepository;
    private final ProductService productService;

    public List<Sale> getAllSales(User user, OauthUser oauthUser) {
        if (user != null) {
            return saleRepository.findAllByUserOrderBySalesDateDesc(user);
        } else if (oauthUser != null) {
            return saleRepository.findAllByOauthUserOrderBySalesDateDesc(oauthUser);
        }
        return List.of();
    }

    public Sale getSaleById(Long id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));
    }

    @Transactional
    public Sale createSale(SaleDTO dto, User user, OauthUser oauthUser) {
        if (user == null && oauthUser == null) {
            throw new IllegalArgumentException("User context must be provided.");
        }

        // Get the product
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + dto.getProductId()));

        // Check if there's enough inventory
        if (product.getQuantity() < dto.getQuantity()) {
            throw new IllegalArgumentException("Not enough inventory. Available: " + product.getQuantity());
        }

        // Get the pricing for the product (first available pricing for the product)
        Optional<Pricing> pricingOptional;
        List<Pricing> productPricings = pricingRepository.findAllByProductId(product.getId());
        if (productPricings.isEmpty()) {
            throw new ResourceNotFoundException("No pricing found for this product");
        }
        Pricing pricing = productPricings.get(0);

        // Calculate total price
        BigDecimal price = getProductPrice(product.getId());
        BigDecimal totalPrice;
        if (dto.getTotalPrice() != null) {
            totalPrice = dto.getTotalPrice();
        } else {
            totalPrice = price.multiply(BigDecimal.valueOf(dto.getQuantity()));
        }

        // Use sale date as is - already LocalDate
        LocalDate salesDate = dto.getSalesDate();
        if (salesDate == null) {
            salesDate = LocalDate.now(); // Default to current date if not provided
        }

        // Create sale record
        Sale sale = new Sale(
                pricing,
                product,
                dto.getCustomerName(),
                dto.getQuantity(),
                price,
                totalPrice,
                salesDate,
                dto.getStatus(),
                user,
                oauthUser
        );

        // Update product inventory by reducing the quantity
        product.setQuantity(product.getQuantity() - dto.getQuantity());
        productRepository.save(product);

        return saleRepository.save(sale);
    }

    @Transactional
    public void deleteSale(Long id) {
        Sale sale = getSaleById(id);

        // Revert inventory change
        Product product = sale.getProduct();
        product.setQuantity(product.getQuantity() + sale.getQuantity());
        productRepository.save(product);

        saleRepository.deleteById(id);
    }

    /**
     * Retrieves the sale price of a product
     * @param productId The product ID to get the price for
     * @return The sale price of the product
     */
    public BigDecimal getProductPrice(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // Access the selling price from the product
        return product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
    }
}

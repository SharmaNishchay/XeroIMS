package com.darkhex.xeroims.service;

import com.darkhex.xeroims.dto.ProductDTO;
import com.darkhex.xeroims.exception.DuplicateProductException;
import com.darkhex.xeroims.exception.ResourceNotFoundException;
import com.darkhex.xeroims.model.*;
import com.darkhex.xeroims.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final UserService userService;
    private final OauthUserService oauthUserService;

    public List<Product> getAllProducts(User user, OauthUser oauthUser) {
        if (user != null) {
            return productRepository.findAllByUserOrderByNameAsc(user);
        } else if (oauthUser != null) {
            return productRepository.findAllByOauthUserOrderByNameAsc(oauthUser);
        }
        return List.of();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Transactional
    public Product addProduct(ProductDTO dto, User user, OauthUser oauthUser) {
        if (user == null && oauthUser == null) {
            throw new IllegalArgumentException("User context must be provided.");
        }

        // Check SKU uniqueness
        Optional<Product> existingProduct;
        if (user != null) {
            existingProduct = productRepository.findBySkuAndUser(dto.getSku(), user);
        } else {
            existingProduct = productRepository.findBySkuAndOauthUser(dto.getSku(), oauthUser);
        }
        if (existingProduct.isPresent()) {
            throw new DuplicateProductException("Product with SKU already exists: " + dto.getSku());
        }

        // Fetch category and check ownership
        Category category = categoryService.getCategoryById(dto.getCategoryId());
        if (user != null && !user.equals(category.getUser())) {
            throw new ResourceNotFoundException("Category not found for this user.");
        }
        if (oauthUser != null && !oauthUser.equals(category.getOauthUser())) {
            throw new ResourceNotFoundException("Category not found for this OAuth user.");
        }

        Product product = new Product();
        product.setName(dto.getName());
        product.setSku(dto.getSku());
        product.setPrice(dto.getPrice());
        product.setQuantity(dto.getQuantity());
        product.setCategory(category);
        product.setUser(user);
        product.setOauthUser(oauthUser);

        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, ProductDTO dto) {
        Product existingProduct = getProductById(id);

        // Check SKU uniqueness (allow if same product)
        Optional<Product> productBySku;
        if (existingProduct.getUser() != null) {
            productBySku = productRepository.findBySkuAndUser(dto.getSku(), existingProduct.getUser());
        } else {
            productBySku = productRepository.findBySkuAndOauthUser(dto.getSku(), existingProduct.getOauthUser());
        }
        if (productBySku.isPresent() && !productBySku.get().getId().equals(id)) {
            throw new DuplicateProductException("Product with SKU already exists: " + dto.getSku());
        }

        // Fetch category and check ownership
        Category category = categoryService.getCategoryById(dto.getCategoryId());
        if (existingProduct.getUser() != null && !existingProduct.getUser().equals(category.getUser())) {
            throw new ResourceNotFoundException("Category not found for this user.");
        }
        if (existingProduct.getOauthUser() != null && !existingProduct.getOauthUser().equals(category.getOauthUser())) {
            throw new ResourceNotFoundException("Category not found for this OAuth user.");
        }

        existingProduct.setName(dto.getName());
        existingProduct.setSku(dto.getSku());
        existingProduct.setPrice(dto.getPrice());
        existingProduct.setQuantity(dto.getQuantity());
        existingProduct.setCategory(category);

        return productRepository.save(existingProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }
}

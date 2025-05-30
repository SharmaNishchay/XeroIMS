package com.darkhex.xeroims.service;

import com.darkhex.xeroims.dto.SupplierDTO;
import com.darkhex.xeroims.exception.DuplicateSupplierException;
import com.darkhex.xeroims.exception.ResourceNotFoundException;
import com.darkhex.xeroims.model.Category;
import com.darkhex.xeroims.model.OauthUser;
import com.darkhex.xeroims.model.Supplier;
import com.darkhex.xeroims.model.User;
import com.darkhex.xeroims.repository.SupplierRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final CategoryService categoryService;

    public List<Supplier> getAllSuppliers(User user, OauthUser oauthUser) {
        if (user != null) {
            return supplierRepository.findAllByUserOrderByNameAsc(user);
        } else if (oauthUser != null) {
            return supplierRepository.findAllByOauthUserOrderByNameAsc(oauthUser);
        }
        return List.of();
    }

    public Supplier getSupplierById(Long id) {
        return supplierRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
    }

    @Transactional
    public Supplier addSupplier(SupplierDTO dto, User user, OauthUser oauthUser) {
        if (user == null && oauthUser == null) {
            throw new IllegalArgumentException("User context must be provided.");
        }

        // Check uniqueness of email and name within user scope
        if (user != null) {
            if (supplierRepository.existsByEmailAndUser(dto.getEmail(), user)) {
                throw new DuplicateSupplierException("Supplier with email already exists: " + dto.getEmail());
            }
            if (supplierRepository.existsByNameAndUser(dto.getName(), user)) {
                throw new DuplicateSupplierException("Supplier with name already exists: " + dto.getName());
            }
        } else {
            if (supplierRepository.existsByEmailAndOauthUser(dto.getEmail(), oauthUser)) {
                throw new DuplicateSupplierException("Supplier with email already exists: " + dto.getEmail());
            }
            if (supplierRepository.existsByNameAndOauthUser(dto.getName(), oauthUser)) {
                throw new DuplicateSupplierException("Supplier with name already exists: " + dto.getName());
            }
        }

        Category category = categoryService.getCategoryById(dto.getCategoryId());

        if (user != null && !user.equals(category.getUser())) {
            throw new ResourceNotFoundException("Category not found for this user.");
        }
        if (oauthUser != null && !oauthUser.equals(category.getOauthUser())) {
            throw new ResourceNotFoundException("Category not found for this OAuth user.");
        }

        Supplier supplier = new Supplier();
        supplier.setName(dto.getName());
        supplier.setEmail(dto.getEmail());
        supplier.setPhone(dto.getPhone());
        supplier.setAddress(dto.getAddress());
        supplier.setCategory(category);
        supplier.setUser(user);
        supplier.setOauthUser(oauthUser);

        return supplierRepository.save(supplier);
    }

    @Transactional
    public Supplier updateSupplier(Long id, SupplierDTO dto) {
        Supplier existingSupplier = getSupplierById(id);

        // Check uniqueness for email and name except current supplier
        if (existingSupplier.getUser() != null) {
            Optional<Supplier> byEmail = supplierRepository.findByEmailAndUser(dto.getEmail(), existingSupplier.getUser());
            if (byEmail.isPresent() && !byEmail.get().getId().equals(id)) {
                throw new DuplicateSupplierException("Supplier with email already exists: " + dto.getEmail());
            }
            Optional<Supplier> byName = supplierRepository.findByNameAndUser(dto.getName(), existingSupplier.getUser());
            if (byName.isPresent() && !byName.get().getId().equals(id)) {
                throw new DuplicateSupplierException("Supplier with name already exists: " + dto.getName());
            }
        } else if (existingSupplier.getOauthUser() != null) {
            Optional<Supplier> byEmail = supplierRepository.findByEmailAndOauthUser(dto.getEmail(), existingSupplier.getOauthUser());
            if (byEmail.isPresent() && !byEmail.get().getId().equals(id)) {
                throw new DuplicateSupplierException("Supplier with email already exists: " + dto.getEmail());
            }
            Optional<Supplier> byName = supplierRepository.findByNameAndOauthUser(dto.getName(), existingSupplier.getOauthUser());
            if (byName.isPresent() && !byName.get().getId().equals(id)) {
                throw new DuplicateSupplierException("Supplier with name already exists: " + dto.getName());
            }
        }

        Category category = categoryService.getCategoryById(dto.getCategoryId());

        if (existingSupplier.getUser() != null && !existingSupplier.getUser().equals(category.getUser())) {
            throw new ResourceNotFoundException("Category not found for this user.");
        }
        if (existingSupplier.getOauthUser() != null && !existingSupplier.getOauthUser().equals(category.getOauthUser())) {
            throw new ResourceNotFoundException("Category not found for this OAuth user.");
        }

        existingSupplier.setName(dto.getName());
        existingSupplier.setEmail(dto.getEmail());
        existingSupplier.setPhone(dto.getPhone());
        existingSupplier.setAddress(dto.getAddress());
        existingSupplier.setCategory(category);

        return supplierRepository.save(existingSupplier);
    }

    @Transactional
    public void deleteSupplier(Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new ResourceNotFoundException("Supplier not found with id: " + id);
        }
        supplierRepository.deleteById(id);
    }
}

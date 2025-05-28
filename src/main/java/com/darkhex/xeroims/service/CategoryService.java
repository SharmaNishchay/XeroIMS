package com.darkhex.xeroims.service;

import com.darkhex.xeroims.exception.DuplicateCategoryException;
import com.darkhex.xeroims.exception.ResourceNotFoundException;
import com.darkhex.xeroims.model.Category;
import com.darkhex.xeroims.model.OauthUser;
import com.darkhex.xeroims.model.User;
import com.darkhex.xeroims.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired private CategoryRepository categoryRepository;

    public List<Category> getAllCategories(User user, OauthUser oauthUser) {
        if (user != null) {
            return categoryRepository.findAllByUserOrderByNameAsc(user);
        } else if (oauthUser != null) {
            return categoryRepository.findAllByOauthUserOrderByNameAsc(oauthUser);
        }
        return List.of();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    @Transactional
    public Category addCategory(Category category, User user, OauthUser oauthUser) {
        if (user == null && oauthUser == null) {
            throw new IllegalArgumentException("User context must be provided.");
        }

        Optional<Category> existingCategory;
        if (user != null) {
            existingCategory = categoryRepository.findByNameAndUser(category.getName(), user);
            category.setUser(user);
        } else {
            existingCategory = categoryRepository.findByNameAndOauthUser(category.getName(), oauthUser);
            category.setOauthUser(oauthUser);
        }

        if (existingCategory.isPresent()) {
            throw new DuplicateCategoryException("Category already exists with name: " + category.getName());
        }

        return categoryRepository.save(category);
    }


    @Transactional
    public Category updateCategory(Long id, Category updatedCategory) {
        Category existingCategory = getCategoryById(id);

        Optional<Category> categoryByName;
        if (existingCategory.getUser() != null) {
            categoryByName = categoryRepository.findByNameAndUser(updatedCategory.getName(), existingCategory.getUser());
        } else {
            categoryByName = categoryRepository.findByNameAndOauthUser(updatedCategory.getName(), existingCategory.getOauthUser());
        }

        if (categoryByName.isPresent() && !categoryByName.get().getId().equals(id)) {
            throw new DuplicateCategoryException("Category already exists with name: " + updatedCategory.getName());
        }

        existingCategory.setName(updatedCategory.getName());
        existingCategory.setDescription(updatedCategory.getDescription());
        return categoryRepository.save(existingCategory);
    }


    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }
}

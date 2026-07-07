package com.bazarak.service;

import com.bazarak.entity.Category;
import com.bazarak.exception.category.*;
import com.bazarak.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    // Create a new category
    public Category createCategory(String name, String description, Long parentId) {
        // Check for duplicate name
        if (categoryRepository.existsByName(name)) {
            throw new CategoryNameAlreadyExistsException("Category with name '" + name + "' already exists");
        }

        Category category = new Category();
        category.setName(name);
        category.setDescription(description);

        // Set parent category if provided
        if (parentId != null) {
            Category parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new CategoryNotFoundException("Parent category not found with id: " + parentId));
            category.setParentCategory(parent);
        }

        return categoryRepository.save(category);
    }

    // Get all categories
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // Get root categories
    public List<Category> getRootCategories() {
        return categoryRepository.findRootCategories();
    }

    // Get sub-categories of a parent
    public List<Category> getSubCategories(Long parentId) {
        // Check if parent exists
        if (!categoryRepository.existsById(parentId)) {
            throw new CategoryNotFoundException("Parent category not found with id: " + parentId);
        }
        return categoryRepository.findByParentCategoryId(parentId);
    }

    // Get category by ID
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + id));
    }

    // Get category by name
    public Category getCategoryByName(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with name: " + name));
    }



    // Search categories
    public List<Category> searchCategories(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllCategories();
        }
        return categoryRepository.searchByName(keyword.trim());
    }
}
package com.bazarak.service;

import com.bazarak.entity.Category;
import com.bazarak.entity.CategorySpecification;
import com.bazarak.exception.category.*;
import com.bazarak.repository.CategoryRepository;
import com.bazarak.repository.CategorySpecificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategorySpecificationRepository categorySpecificationRepository;

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

    // Update category
    public Category updateCategory(Long id, String name, String description, Long parentId) {
        Category category = getCategoryById(id);

        // Check if name is being changed and is unique
        if (name != null && !name.equals(category.getName())) {
            if (categoryRepository.existsByName(name)) {
                throw new CategoryNameAlreadyExistsException("Category with name '" + name + "' already exists");
            }
            category.setName(name);
        }

        if (description != null) {
            category.setDescription(description);
        }

        // Update parent relationship
        if (parentId != null) {
            // Check if parent exists
            Category parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new CategoryNotFoundException("Parent category not found with id: " + parentId));

            // Prevent circular reference (category cannot be its own parent)
            if (parentId.equals(id)) {
                throw new CategoryNameAlreadyExistsException("A category cannot be its own parent");
            }

            // Prevent circular reference (cannot set parent to a sub-category)
            if (isAncestor(id, parentId)) {
                throw new CategoryNameAlreadyExistsException("Cannot set parent to a sub-category (would create circular reference)");
            }

            category.setParentCategory(parent);
        } else {
            category.setParentCategory(null);
        }

        return categoryRepository.save(category);
    }

    // Helper method to check if a category is ancestor of another
    private boolean isAncestor(Long ancestorId, Long categoryId) {
        Category current = getCategoryById(categoryId);
        while (current.getParentCategory() != null) {
            if (current.getParentCategory().getId().equals(ancestorId)) {
                return true;
            }
            current = current.getParentCategory();
        }
        return false;
    }

    // Delete category
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);

        // Check if category has sub-categories
        if (category.hasSubCategories()) {
            throw new CategoryHasSubCategoriesException("Cannot delete category with sub-categories. Delete sub-categories first.");
        }

        // Check if category has advertisements
        if (!category.getAdvertisements().isEmpty()) {
            throw new CategoryHasAdvertisementsException("Cannot delete category with advertisements. Re-assign them first.");
        }

        categoryRepository.delete(category);
    }



    // Search categories
    public List<Category> searchCategories(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllCategories();
        }
        return categoryRepository.searchByName(keyword.trim());
    }

    /**
     * Add a new specification to a category (Admin only)
     */
    @Transactional
    public CategorySpecification addSpecificationToCategory(
            Long categoryId,
            String name,
            String type,
            String options,
            boolean required) {

        Category category = getCategoryById(categoryId);

        // Validate
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidInputException("Specification name is required");
        }

        CategorySpecification.SpecType specType;
        try {
            specType = CategorySpecification.SpecType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid specification type. Valid: TEXT, NUMBER, BOOLEAN, DROPDOWN");
        }

        // Validate options for DROPDOWN type
        if (specType == CategorySpecification.SpecType.DROPDOWN &&
                (options == null || options.trim().isEmpty())) {
            throw new InvalidInputException("Options are required for DROPDOWN type");
        }

        CategorySpecification spec = new CategorySpecification();
        spec.setName(name.trim());
        spec.setType(specType);
        spec.setOptions(options != null ? options.trim() : null);
        spec.setRequired(required);
        spec.setCategory(category);

        return categorySpecificationRepository.save(spec);
    }

    /**
     * Get all specifications for a category (including inherited from parent)
     * This implements the Divar-style: Layer 2 holds specs, Layer 3 inherits
     */
    public List<CategorySpecification> getSpecificationsForCategory(Long categoryId) {
        Category category = getCategoryById(categoryId);

        // If this category has its own specs → use them
        if (category.hasSpecifications()) {
            return category.getSpecifications();
        }

        // If not, check if parent has specs (Layer 2)
        if (category.getParentCategory() != null &&
                category.getParentCategory().hasSpecifications()) {
            return category.getParentCategory().getSpecifications();
        }

        // If still not, check grandparent (Layer 1)
        if (category.getParentCategory() != null &&
                category.getParentCategory().getParentCategory() != null &&
                category.getParentCategory().getParentCategory().hasSpecifications()) {
            return category.getParentCategory().getParentCategory().getSpecifications();
        }

        // No specifications found
        return new ArrayList<>();
    }

    /**
     * Get specifications directly attached to a category (no inheritance)
     */
    public List<CategorySpecification> getDirectSpecifications(Long categoryId) {
        return categorySpecificationRepository.findByCategoryId(categoryId);
    }
}
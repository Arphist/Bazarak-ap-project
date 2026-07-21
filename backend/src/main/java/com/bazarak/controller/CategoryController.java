package com.bazarak.controller;

import com.bazarak.entity.Category;
import com.bazarak.entity.CategorySpecification;
import com.bazarak.service.CategoryService;
import com.bazarak.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    /**
     * Retrieves all categories.
     *
     * @return list of all categories
     */
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    /**
     * Retrieves root categories (categories without a parent).
     *
     * @return list of root categories
     */
    @GetMapping("/roots")
    public ResponseEntity<List<Category>> getRootCategories() {
        return ResponseEntity.ok(categoryService.getRootCategories());
    }

    /**
     * Retrieves sub-categories of a specific category.
     *
     * @param id the ID of the parent category
     * @return list of sub-categories
     */
    @GetMapping("/{id}/subcategories")
    public ResponseEntity<List<Category>> getSubCategories(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getSubCategories(id));
    }

    /**
     * Retrieves a category by its ID.
     *
     * @param id the ID of the category
     * @return the category with the given ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    /**
     * Searches for categories by keyword in their name.
     *
     * @param keyword the search keyword (optional)
     * @return list of categories matching the search criteria
     */
    @GetMapping("/search")
    public ResponseEntity<List<Category>> searchCategories(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(categoryService.searchCategories(keyword));
    }
    /**
     * Creates a new category. Admin only.
     *
     * @param request the category creation request DTO
     * @param session the HTTP session for admin validation
     * @return the created category
     */
    @PostMapping
    public ResponseEntity<Category> createCategory(
            @Valid @RequestBody CategoryRequest request,
            HttpSession session) {

        userService.checkAdmin(session);

        Category category = categoryService.createCategory(
                request.getName(),
                request.getDescription(),
                request.getParentId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    /**
     * Updates an existing category. Admin only.
     *
     * @param id      the ID of the category to update
     * @param request the category update request DTO
     * @param session the HTTP session for admin validation
     * @return the updated category
     */
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request,
            HttpSession session) {

        userService.checkAdmin(session);

        Category category = categoryService.updateCategory(
                id,
                request.getName(),
                request.getDescription(),
                request.getParentId()
        );
        return ResponseEntity.ok(category);
    }

    /**
     * Deletes a category by its ID. Admin only.
     *
     * @param id      the ID of the category to delete
     * @param session the HTTP session for admin validation
     * @return a success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteCategory(
            @PathVariable Long id,
            HttpSession session) {

        userService.checkAdmin(session);

        categoryService.deleteCategory(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Category deleted successfully");
        return ResponseEntity.ok(response);
    }

    // SPECIFICATION MANAGEMENT (Admin Only)

    /**
     * Get all specifications for a category (including inherited)
     */
    @GetMapping("/{categoryId}/specifications")
    public ResponseEntity<List<CategorySpecification>> getCategorySpecifications(
            @PathVariable Long categoryId) {
        List<CategorySpecification> specs = categoryService.getSpecificationsForCategory(categoryId);
        return ResponseEntity.ok(specs);
    }

    /**
     * Get direct specifications for a category (no inheritance)
     */
    @GetMapping("/{categoryId}/specifications/direct")
    public ResponseEntity<List<CategorySpecification>> getDirectSpecifications(
            @PathVariable Long categoryId) {
        List<CategorySpecification> specs = categoryService.getDirectSpecifications(categoryId);
        return ResponseEntity.ok(specs);
    }

    /**
     * Add a specification to a category (Admin only)
     */
    @PostMapping("/{categoryId}/specifications")
    public ResponseEntity<CategorySpecification> addSpecificationToCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody SpecificationRequest request,
            HttpSession session) {

        userService.checkAdmin(session);

        CategorySpecification spec = categoryService.addSpecificationToCategory(
                categoryId,
                request.getName(),
                request.getType(),
                request.getOptions(),
                request.isRequired()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(spec);
    }

    /**
     * Update a specification (Admin only)
     */
    @PutMapping("/specifications/{specId}")
    public ResponseEntity<CategorySpecification> updateSpecification(
            @PathVariable Long specId,
            @Valid @RequestBody SpecificationRequest request,
            HttpSession session) {

        userService.checkAdmin(session);

        CategorySpecification spec = categoryService.updateSpecification(
                specId,
                request.getName(),
                request.getType(),
                request.getOptions(),
                request.isRequired()
        );

        return ResponseEntity.ok(spec);
    }

    /**
     * Delete a specification (Admin only)
     */
    @DeleteMapping("/specifications/{specId}")
    public ResponseEntity<?> deleteSpecification(
            @PathVariable Long specId,
            HttpSession session) {

        userService.checkAdmin(session);

        categoryService.deleteSpecification(specId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Specification deleted successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * DTO for category creation and update requests.
     */
    public static class CategoryRequest {
        private String name;
        private String description;
        private Long parentId;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Long getParentId() {
            return parentId;
        }

        public void setParentId(Long parentId) {
            this.parentId = parentId;
        }
    }


}
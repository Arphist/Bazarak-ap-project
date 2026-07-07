package com.bazarak.controller;

import com.bazarak.entity.Category;
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
@RequestMapping("/api/categories")
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


}
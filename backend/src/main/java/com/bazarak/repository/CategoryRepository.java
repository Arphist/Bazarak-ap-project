package com.bazarak.repository;

import com.bazarak.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Find category by name (for uniqueness check)
    Optional<Category> findByName(String name);

    // Check if category name already exists
    boolean existsByName(String name);

    // Get all root categories (without parent)
    @Query("SELECT c FROM Category c WHERE c.parentCategory IS NULL ORDER BY c.name ASC")
    List<Category> findRootCategories();

    // Get all sub-categories of a specific category
    List<Category> findByParentCategoryId(Long parentId);

    // Search categories by name keyword (case-insensitive)
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY c.name ASC")
    List<Category> searchByName(@Param("keyword") String keyword);
}
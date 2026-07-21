package com.bazarak.repository;

import com.bazarak.entity.CategorySpecification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategorySpecificationRepository extends JpaRepository<CategorySpecification, Long> {

    /**
     * Find all specifications for a specific category
     */
    List<CategorySpecification> findByCategoryId(Long categoryId);

    /**
     * Find all specifications for a category and its sub-categories (for inheritance)
     */
    @Query("SELECT s FROM CategorySpecification s WHERE s.category.id = :categoryId OR s.category.parentCategory.id = :categoryId")
    List<CategorySpecification> findByCategoryOrParent(@Param("categoryId") Long categoryId);

    /**
     * Check if a category has specifications
     */
    boolean existsByCategoryId(Long categoryId);

    /**
     * Delete all specifications for a category
     */
    void deleteByCategoryId(Long categoryId);
}
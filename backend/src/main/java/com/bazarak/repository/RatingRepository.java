package com.bazarak.repository;

import com.bazarak.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    /**
     * Find all ratings given to a specific seller.
     */
    List<Rating> findBySellerId(Long sellerId);

    /**
     * Find all ratings given to a specific seller, ordered by creation date descending.
     */
    @Query("SELECT r FROM Rating r WHERE r.seller.id = :sellerId ORDER BY r.createdAt DESC")
    List<Rating> findBySellerIdOrderByCreatedAtDesc(@Param("sellerId") Long sellerId);

    /**
     * Find all ratings given by a specific buyer.
     */
    List<Rating> findByBuyerId(Long buyerId);

    /**
     * Find all ratings for a specific advertisement.
     */
    List<Rating> findByAdvertisementId(Long advertisementId);

    /**
     * Check if a rating already exists for a buyer, seller, and advertisement.
     */
    boolean existsByBuyerIdAndSellerIdAndAdvertisementId(Long buyerId, Long sellerId, Long advertisementId);

    /**
     * Calculate average score for a seller.
     */
    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.seller.id = :sellerId")
    Double calculateAverageScoreBySellerId(@Param("sellerId") Long sellerId);

    /**
     * Count total ratings for a seller.
     */
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.seller.id = :sellerId")
    Long countBySellerId(@Param("sellerId") Long sellerId);

    /**
     * Find a specific rating by buyer, seller, and advertisement.
     */
    Optional<Rating> findByBuyerIdAndSellerIdAndAdvertisementId(Long buyerId, Long sellerId, Long advertisementId);
}
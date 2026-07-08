package com.bazarak.repository;

import com.bazarak.entity.Advertisement;
import com.bazarak.entity.Advertisement.AdStatus;
import com.bazarak.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdRepository extends JpaRepository<Advertisement, Long> {

    // BASIC FIND METHODS (Spring Data JPA generates these)

    /**
     * Find all ads by status
     */
    List<Advertisement> findByStatus(AdStatus status);

    /**
     * Find all active ads (visible to everyone)
     */
    List<Advertisement> findByStatusOrderByCreatedAtDesc(AdStatus status);

    /**
     * Find ads by owner
     */
    List<Advertisement> findByOwner(User owner);

    /**
     * Find ads by owner and status
     */
    List<Advertisement> findByOwnerAndStatus(User owner, AdStatus status);

    /**
     * Find ads by category
     */
    List<Advertisement> findByCategoryId(Long categoryId);

    /**
     * Find ads by city
     */
    List<Advertisement> findByCityId(Long cityId);

    /**
     * Find ads by price range
     */
    List<Advertisement> findByPriceBetween(Long minPrice, Long maxPrice);

    /**
     * Find ads by title containing keyword (case in-sensitive)
     */
    List<Advertisement> findByTitleContainingIgnoreCase(String keyword);

    /**
     * Count ads by status
     */
    long countByStatus(AdStatus status);

    /**
     * Check if ad exists and belongs to user
     */
    boolean existsByIdAndOwner(Long id, User owner);

    /**
     * Check if ad exists and has a specific status
     */
    boolean existsByIdAndStatus(Long id, AdStatus status);

    // CUSTOM JPQL QUERIES

    /**
     * Find all active ads with their owner, category, and city loaded (eager fetch)
     */
    @Query("SELECT a FROM Advertisement a " +
            "LEFT JOIN FETCH a.owner " +
            "LEFT JOIN FETCH a.category " +
            "LEFT JOIN FETCH a.city " +
            "WHERE a.status = :status " +
            "ORDER BY a.createdAt DESC")
    List<Advertisement> findActiveAdsWithDetails(@Param("status") AdStatus status);

    /**
     * Find pending ads for admin review (with owner loaded)
     */
    @Query("SELECT a FROM Advertisement a " +
            "LEFT JOIN FETCH a.owner " +
            "WHERE a.status = :status " +
            "ORDER BY a.createdAt ASC")
    List<Advertisement> findPendingAdsWithOwner(@Param("status") AdStatus status);

    /**
     * Search ads by title or description (case in-sensitive)
     */
    @Query("SELECT a FROM Advertisement a " +
            "WHERE a.status = :status " +
            "AND (LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY a.createdAt DESC")
    List<Advertisement> searchActiveAds(@Param("keyword") String keyword, @Param("status") AdStatus status);

    /**
     * Search with all filters (no Specification needed!)
     */
    @Query("SELECT a FROM Advertisement a " +
            "WHERE a.status = :status " +
            "AND (:keyword IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:categoryId IS NULL OR a.category.id = :categoryId) " +
            "AND (:cityId IS NULL OR a.city.id = :cityId) " +
            "AND (:minPrice IS NULL OR a.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR a.price <= :maxPrice)")
    List<Advertisement> searchAdsWithFilters(@Param("status") AdStatus status,
                                          @Param("keyword") String keyword,
                                          @Param("categoryId") Long categoryId,
                                          @Param("cityId") Long cityId,
                                          @Param("minPrice") Long minPrice,
                                          @Param("maxPrice") Long maxPrice);

    // UPDATE QUERIES (Modifying)

    /**
     * Update ad status (for admin approval/rejection)
     */
    @Modifying
    @Transactional
    @Query("UPDATE Advertisement a SET a.status = :status, a.approvedAt = :approvedAt WHERE a.id = :adId")
    int updateAdStatus(@Param("adId") Long adId,
                       @Param("status") AdStatus status,
                       @Param("approvedAt") LocalDateTime approvedAt);

    /**
     * Mark ad as sold
     */
    @Modifying
    @Transactional
    @Query("UPDATE Advertisement a SET a.status = 'SOLD', a.soldAt = :soldAt WHERE a.id = :adId AND a.owner.id = :ownerId")
    int markAsSold(@Param("adId") Long adId,
                   @Param("ownerId") Long ownerId,
                   @Param("soldAt") LocalDateTime soldAt);

    /**
     * Soft delete ad (set status to DELETED)
     */
    @Modifying
    @Transactional
    @Query("UPDATE Advertisement a SET a.status = 'DELETED' WHERE a.id = :adId")
    int softDeleteAd(@Param("adId") Long adId);

    /**
     * Restore a soft-deleted ad
     */
    @Modifying
    @Transactional
    @Query("UPDATE Advertisement a SET a.status = :status WHERE a.id = :adId")
    int restoreAd(@Param("adId") Long adId, @Param("status") AdStatus status);

    // NATIVE SQL QUERIES (Direct SQL)

    /**
     * Get total number of active ads
     */
    @Query(value = "SELECT COUNT(*) FROM ads WHERE status = 'ACTIVE'", nativeQuery = true)
    long countActiveAds();

    /**
     * Get all ads by a specific user with images loaded (native query)
     */
    @Query(value = "SELECT a.* FROM ads a WHERE a.owner_id = :ownerId AND a.status != 'DELETED'", nativeQuery = true)
    List<Advertisement> findUserAdsNative(@Param("ownerId") Long ownerId);

    // STATISTICS / DASHBOARD QUERIES

    /**
     * Get count of ads by status
     */
    @Query("SELECT a.status, COUNT(a) FROM Advertisement a GROUP BY a.status")
    List<Object[]> countAdsByStatus();

    /**
     * Get count of ads by category
     */
    @Query("SELECT c.name, COUNT(a) FROM Advertisement a JOIN a.category c GROUP BY c.name ORDER BY COUNT(a) DESC")
    List<Object[]> countAdsByCategory();

    /**
     * Get count of ads by city
     */
    @Query("SELECT c.name, COUNT(a) FROM Advertisement a JOIN a.city c GROUP BY c.name ORDER BY COUNT(a) DESC")
    List<Object[]> countAdsByCity();

    /**
     * Get ads created in the last N days
     */
    @Query("SELECT a FROM Advertisement a WHERE a.createdAt >= :since AND a.status = :status ORDER BY a.createdAt DESC")
    List<Advertisement> findRecentAds(@Param("since") LocalDateTime since, @Param("status") AdStatus status);

    /**
     * Get top expensive active ads
     */
    @Query("SELECT a FROM Advertisement a WHERE a.status = :status ORDER BY a.price DESC")
    List<Advertisement> findMostExpensiveAds(@Param("status") AdStatus status);

    /**
     * Get ads that have been active for the longest time
     */
    @Query("SELECT a FROM Advertisement a WHERE a.status = 'ACTIVE' ORDER BY a.approvedAt ASC")
    List<Advertisement> findOldestActiveAds();
}
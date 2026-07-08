package com.bazarak.repository;

import com.bazarak.entity.Advertisement;
import com.bazarak.entity.Favorite;
import com.bazarak.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    /**
     * Find all favorites for a user
     */
    List<Favorite> findByUser(User user);

    /**
     * Find all favorites for a user with ad details loaded
     */
    @Query("SELECT f FROM Favorite f " +
            "LEFT JOIN FETCH f.advertisement " +
            "LEFT JOIN FETCH f.advertisement.owner " +
            "LEFT JOIN FETCH f.advertisement.category " +
            "LEFT JOIN FETCH f.advertisement.city " +
            "WHERE f.user = :user " +
            "ORDER BY f.createdAt DESC")
    List<Favorite> findByUserWithAdDetails(@Param("user") User user);

    /**
     * Find a specific favorite by user and ad
     */
    Optional<Favorite> findByUserAndAdvertisement(User user, Advertisement ad);

    /**
     * Check if a user has favorited a specific ad
     */
    boolean existsByUserAndAdvertisement(User user, Advertisement ad);

    /**
     * Delete a favorite by user and ad
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Favorite f WHERE f.user = :user AND f.advertisement = :ad")
    void deleteByUserAndAd(@Param("user") User user, @Param("ad") Advertisement ad);

    /**
     * Count favorites for an ad
     */
    long countByAdvertisement(Advertisement ad);

    /**
     * Count favorites for a user
     */
    long countByUser(User user);
}
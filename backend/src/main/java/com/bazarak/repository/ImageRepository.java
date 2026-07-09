package com.bazarak.repository;

import com.bazarak.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    /**
     * Find all images for an ad
     */
    List<Image> findByAdvertisementIdOrderByDisplayOrderAsc(Long adId);

    /**
     * Find primary image for an ad
     */
    @Query("SELECT i FROM Image i WHERE i.advertisement.id = :adId AND i.isPrimary = true")
    Image findPrimaryImageByAdId(@Param("adId") Long adId);

    /**
     * Count images for an ad
     */
    long countByAdvertisementId(Long adId);

    /**
     * Delete all images for an ad
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Image i WHERE i.advertisement.id = :adId")
    void deleteByAdvertisementId(@Param("adId") Long adId);
}

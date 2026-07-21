package com.bazarak.repository;

import com.bazarak.entity.AdvertisementSpecification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdvertisementSpecificationRepository extends JpaRepository<AdvertisementSpecification, Long> {

    /**
     * Find all specification values for an advertisement
     */
    List<AdvertisementSpecification> findByAdvertisementId(Long advertisementId);

    /**
     * Find a specific specification value for an advertisement
     */
    Optional<AdvertisementSpecification> findByAdvertisementIdAndSpecificationId(Long advertisementId, Long specificationId);

    /**
     * Delete all specification values for an advertisement
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM AdvertisementSpecification a WHERE a.advertisement.id = :advertisementId")
    void deleteByAdvertisementId(@Param("advertisementId") Long advertisementId);

    /**
     * Count specifications for an advertisement
     */
    long countByAdvertisementId(Long advertisementId);
}
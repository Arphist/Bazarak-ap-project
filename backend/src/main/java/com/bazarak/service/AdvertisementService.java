package com.bazarak.service;

import com.bazarak.entity.Advertisement;
import com.bazarak.entity.Advertisement.AdStatus;
import com.bazarak.entity.User;
import com.bazarak.exception.user.*;
import com.bazarak.exception.advertisement.*;
import com.bazarak.repository.AdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdvertisementService {


    @Autowired
    private AdRepository adRepository;

    @Autowired
    private UserService userService;

    // CREATE ADVERTISEMENT

    /**
     * Create a new advertisement
     * Status is set to PENDING (waiting for admin approval)
     */
    @Transactional
    public Advertisement createAd(Advertisement ad, Long ownerId) {
        // 1. Get the owner
        User owner = userService.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + ownerId));

        // 2. Check if user is active
        if (!owner.isActive()) {
            throw new UserBlockedException("Your account is blocked. You cannot post ads.");
        }

        // 3. Set owner and default values
        ad.setOwner(owner);
        ad.setStatus(AdStatus.PENDING);
        ad.setCreatedAt(LocalDateTime.now());
        ad.setRatingCount(0);
        ad.setAverageRating(0.0);

        // 4. Validate price is positive
        if (ad.getPrice() == null || ad.getPrice() <= 0) {
            throw new InvalidPriceInputException("Price must be positive");
        }

        // 5. Save and return
        return adRepository.save(ad);
    }

    // FIND METHODS

    /**
     * Find advertisement by ID
     */
    public Advertisement findById(Long id) {
        return adRepository.findById(id)
                .orElseThrow(() -> new AdNotFoundException("Advertisement not found with id: " + id));
    }

    /**
     * Get all ads (admin only)
     */
    public List<Advertisement> findAllAds() {
        return adRepository.findAll();
    }

    /**
     * Get all active ads (visible to everyone)
     */
    public List<Advertisement> getActiveAds() {
        return adRepository.findByStatusOrderByCreatedAtDesc(AdStatus.ACCEPTED);
    }

    /**
     * Get all active ads with details (owner, category, city loaded)
     */
    public List<Advertisement> getActiveAdsWithDetails() {
        return adRepository.findActiveAdsWithDetails(AdStatus.ACCEPTED);
    }

    /**
     * Get pending ads (admin only)
     */
    public List<Advertisement> getPendingAds() {
        return adRepository.findPendingAdsWithOwner(AdStatus.PENDING);
    }

    /**
     * Get ads by owner
     */
    public List<Advertisement> getAdsByOwner(Long ownerId) {
        User owner = userService.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return adRepository.findByOwner(owner);
    }

    /**
     * Get ads by status
     */
    public List<Advertisement> getAdsByStatus(AdStatus status) {
        return adRepository.findByStatus(status);
    }

    /**
     * Get ads by category
     */
    public List<Advertisement> getAdsByCategory(Long categoryId) {
        return adRepository.findByCategoryId(categoryId);
    }

    /**
     * Get ads by city
     */
    public List<Advertisement> getAdsByCity(Long cityId) {
        return adRepository.findByCityId(cityId);
    }

    // SEARCH METHODS

    /**
     * Search ads by keyword (title or description)
     */
    public List<Advertisement> searchAds(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getActiveAdsWithDetails();
        }
        return adRepository.searchActiveAds(keyword.trim(), AdStatus.ACCEPTED);
    }

    /**
     * Advanced search with filters
     */
    public List<Advertisement> searchAdsWithFilters(String keyword, Long categoryId, Long cityId,
                                                    Long minPrice, Long maxPrice) {
        // If no keyword, search with filters only
        if (keyword == null || keyword.trim().isEmpty()) {
            return adRepository.searchAdsWithFilters(
                    AdStatus.ACCEPTED, categoryId, cityId, minPrice, maxPrice);
        }

        // With keyword: first search by keyword, then filter
        List<Advertisement> results = adRepository.searchActiveAds(keyword.trim(), AdStatus.ACCEPTED);

        // Apply filters in memory
        return results.stream()
                .filter(ad -> categoryId == null || ad.getCategory().getId().equals(categoryId))
                .filter(ad -> cityId == null || ad.getCity().getId().equals(cityId))
                .filter(ad -> minPrice == null || ad.getPrice() >= minPrice)
                .filter(ad -> maxPrice == null || ad.getPrice() <= maxPrice)
                .toList();
    }

    // UPDATE METHODS

    /**
     * Update advertisement (owner only)
     * Only allowed if ad is PENDING or ACCEPTED
     */
    @Transactional
    public Advertisement updateAd(Long adId, Advertisement updatedAd, Long ownerId) {
        // 1. Find existing ad
        Advertisement existingAd = findById(adId);

        // 2. Check ownership
        if (!existingAd.getOwner().getId().equals(ownerId)) {
            throw new UnauthorizedAccessException("You don't own this advertisement");
        }

        // 3. Check if ad is editable
        if (!existingAd.isEditable()) {
            throw new InvalidOperationException("This advertisement cannot be edited (status: " + existingAd.getStatus() + ")");
        }

        // 4. Update fields
        if (updatedAd.getTitle() != null) {
            existingAd.setTitle(updatedAd.getTitle());
        }
        if (updatedAd.getDescription() != null) {
            existingAd.setDescription(updatedAd.getDescription());
        }
        if (updatedAd.getPrice() != null && updatedAd.getPrice() > 0) {
            existingAd.setPrice(updatedAd.getPrice());
        }
        if (updatedAd.getCategory() != null) {
            existingAd.setCategory(updatedAd.getCategory());
        }
        if (updatedAd.getCity() != null) {
            existingAd.setCity(updatedAd.getCity());
        }

        existingAd.setUpdatedAt(LocalDateTime.now());

        return adRepository.save(existingAd);
    }

    // ADMIN METHODS

    /**
     * Approve an advertisement (admin only)
     */
    @Transactional
    public Advertisement approveAd(Long adId) {
        Advertisement ad = findById(adId);

        if (ad.getStatus() != AdStatus.PENDING) {
            throw new InvalidOperationException("Only pending ads can be approved");
        }

        ad.setStatus(AdStatus.ACCEPTED);
        ad.setApprovedAt(LocalDateTime.now());
        ad.setUpdatedAt(LocalDateTime.now());

        return adRepository.save(ad);
    }

    /**
     * Reject an advertisement (admin only)
     */
    @Transactional
    public Advertisement rejectAd(Long adId, String rejectionReason) {
        Advertisement ad = findById(adId);

        if (ad.getStatus() != AdStatus.PENDING) {
            throw new InvalidOperationException("Only pending ads can be rejected");
        }

        ad.setStatus(AdStatus.REJECTED);
        ad.setApprovedAt(LocalDateTime.now());
        ad.setUpdatedAt(LocalDateTime.now());
        ad.setRejectionReason(rejectionReason);

        return adRepository.save(ad);
    }

    /**
     * Delete advertisement (admin only - hard delete)
     */
    @Transactional
    public void deleteAdAdmin(Long adId) {
        Advertisement ad = findById(adId);
        adRepository.delete(ad);
    }

    // OWNER METHODS

    /**
     * Soft delete advertisement (owner)
     */
    @Transactional
    public Advertisement deleteAdUser(Long adId, Long ownerId) {
        Advertisement ad = findById(adId);

        // Check ownership
        if (!ad.getOwner().getId().equals(ownerId)) {
            throw new UnauthorizedAccessException("You don't own this advertisement");
        }

        if (ad.getStatus() == AdStatus.DELETED) {
            throw new InvalidOperationException("This advertisement is already deleted");
        }

        ad.setStatus(AdStatus.DELETED);
        ad.setUpdatedAt(LocalDateTime.now());

        return adRepository.save(ad);
    }

    /**
     * Mark advertisement as sold (owner only)
     */
    @Transactional
    public Advertisement markAsSold(Long adId, Long ownerId) {
        Advertisement ad = findById(adId);

        // Check ownership
        if (!ad.getOwner().getId().equals(ownerId)) {
            throw new UnauthorizedAccessException("You don't own this advertisement");
        }

        if (ad.getStatus() != AdStatus.ACCEPTED) {
            throw new InvalidOperationException("Only active ads can be marked as sold");
        }

        ad.setStatus(AdStatus.SOLD);
        ad.setSoldAt(LocalDateTime.now());
        ad.setUpdatedAt(LocalDateTime.now());

        return adRepository.save(ad);
    }

    // RATING METHODS

    /**
     * Update ad rating (called when a user rates an ad)
     */
    @Transactional
    public Advertisement updateRating(Long adId, int newRating) {
        Advertisement ad = findById(adId);

        // Calculate new average
        int currentCount = ad.getRatingCount();
        double currentAvg = ad.getAverageRating();

        // New average formula: (currentAvg * currentCount + newRating) / (currentCount + 1)
        double newAvg = (currentAvg * currentCount + newRating) / (currentCount + 1);

        ad.setAverageRating(Math.round(newAvg * 100.0) / 100.0); // Round to 2 decimal places
        ad.setRatingCount(currentCount + 1);
        ad.setUpdatedAt(LocalDateTime.now());

        return adRepository.save(ad);
    }

    // STATISTICS

    public long getActiveAdCount() {
        return adRepository.countActiveAds();
    }

    public long getPendingAdCount() {
        return adRepository.countByStatus(AdStatus.PENDING);
    }

    public List<Object[]> getAdsByCategoryStats() {
        return adRepository.countAdsByCategory();
    }

    public List<Object[]> getAdsByCityStats() {
        return adRepository.countAdsByCity();
    }

    public List<Advertisement> getRecentAds(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return adRepository.findRecentAds(since, AdStatus.ACCEPTED);
    }

    public List<Advertisement> getMostExpensiveAds() {
        return adRepository.findMostExpensiveAds(AdStatus.ACCEPTED);
    }

    // VALIDATION HELPERS

    public boolean adExists(Long id) {
        return adRepository.existsById(id);
    }

    public boolean adBelongsToUser(Long adId, Long userId) {
        return adRepository.existsByIdAndOwner(adId,
                userService.findById(userId).orElse(null));
    }

    public boolean isAdActive(Long adId) {
        return adRepository.existsByIdAndStatus(adId, AdStatus.ACCEPTED);
    }
}
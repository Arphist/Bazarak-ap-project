package com.bazarak.service;

import com.bazarak.entity.*;
import com.bazarak.entity.Advertisement.AdStatus;
import com.bazarak.exception.auth.UnauthorizedAccessException;
import com.bazarak.exception.category.InvalidInputException;
import com.bazarak.exception.user.*;
import com.bazarak.exception.advertisement.*;
import com.bazarak.repository.AdRepository;
import com.bazarak.repository.AdvertisementSpecificationRepository;
import com.bazarak.repository.CategorySpecificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdvertisementService {


    @Autowired
    private AdRepository adRepository;

    @Autowired
    private UserService userService;
 @Autowired
    private CategoryService categoryService;
 @Autowired
    private CityService cityService;

    @Autowired
    private CategorySpecificationRepository categorySpecificationRepository;

    @Autowired
    private AdvertisementSpecificationRepository advertisementSpecificationRepository;

    // CREATE ADVERTISEMENT

    /**
     * Create a new advertisement with specification values
     */
    @Transactional
    public Advertisement createAd(Advertisement ad, Long ownerId, Long cityId,
                                  Long categoryId, Map<Long, String> specValues) {
        // 1. Get the owner
        User owner = userService.getUserById(ownerId);

        if (!owner.isActive()) {
            throw new UserBlockedException("Your account is blocked. You cannot post ads.");
        }

        // 2. Get Category and City
        Category category = categoryService.getCategoryById(categoryId);
        City city = cityService.getCityById(cityId);

        // 3. Validate category has specifications (if specs are provided)
        if (specValues != null && !specValues.isEmpty()) {
            categoryService.validateSpecificationValues(categoryId, specValues);
        }

        // 4. Set all fields
        ad.setOwner(owner);
        ad.setCategory(category);
        ad.setCity(city);
        ad.setStatus(AdStatus.PENDING);
        ad.setCreatedAt(LocalDateTime.now());
        ad.setRatingCount(0);
        ad.setAverageRating(0.0);
        ad.setFavoriteCount(0);

        if (ad.getPrice() == null || ad.getPrice() <= 0) {
            throw new InvalidInputException("Price must be positive");
        }

        // 5. Save ad first (to get an ID)
        Advertisement savedAd = adRepository.save(ad);

        // 6. Process specification values
        if (specValues != null && !specValues.isEmpty()) {
            List<CategorySpecification> specs = categoryService.getSpecificationsForCategory(categoryId);

            for (CategorySpecification spec : specs) {
                String value = specValues.get(spec.getId());

                // Skip empty values
                if (value == null || value.trim().isEmpty()) {
                    continue;
                }

                // Create and save specification value
                AdvertisementSpecification adSpec = new AdvertisementSpecification();
                adSpec.setAdvertisement(savedAd);
                adSpec.setSpecification(spec);
                adSpec.setValue(value.trim());
                advertisementSpecificationRepository.save(adSpec);

                // Add to ad's collection
                savedAd.addSpecificationValue(adSpec);
            }
        }

        return savedAd;
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

    public List<Advertisement> getActiveAdsByOwner(Long ownerId) {
        return adRepository.findActiveAdsByOwnerId(ownerId);
    }

    public List<Advertisement> getPendingAdsByOwner(Long ownerId) {
        return adRepository.findPendingAdsByOwnerId(ownerId);
    }

    public List<Advertisement> getRejectedAdsByOwner(Long ownerId) {
        return adRepository.findRejectedAdsByOwnerId(ownerId);
    }

    public List<Advertisement> getSoldAdsByOwner(Long ownerId) {
        return adRepository.findSoldAdsByOwnerId(ownerId);
    }

    public List<Advertisement> getDeletedAdsByOwner(Long ownerId) {
        return adRepository.findDeletedAdsByOwnerId(ownerId);
    }

    public List<Advertisement> getRecentAdsByOwner(Long ownerId) {
        return adRepository.findRecentAdsByOwnerId(ownerId);
    }

    public List<Advertisement> getAdsByStatusAndOwner(Long ownerId, AdStatus status) {
        return adRepository.findAdsByStatusAndOwnerId(status, ownerId);
    }

    public List<Advertisement> getAdsByOwnerWithDetails(Long ownerId) {
        return adRepository.findAdsByOwnerIdWithDetails(ownerId);
    }

    public List<Advertisement> getActiveAdsByOwnerWithDetails(Long ownerId) {
        return adRepository.findActiveAdsByOwnerIdWithDetails(ownerId);
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
        // Get results from database using JPQL
        List<Advertisement> results = adRepository.searchAdsWithFilters(
                AdStatus.ACCEPTED,
                keyword,
                categoryId,
                cityId,
                minPrice,
                maxPrice
        );

        return results;
    }

    public List<Advertisement> applySorting(List<Advertisement> ads, String sortBy, String sortOrder) {
        Comparator<Advertisement> comparator;

        switch (sortBy.toLowerCase()) {
            case "price":
                comparator = Comparator.comparing(Advertisement::getPrice);
                break;
            case "title":
                comparator = Comparator.comparing(Advertisement::getTitle, String.CASE_INSENSITIVE_ORDER);
                break;
            case "created_at":
            default:
                comparator = Comparator.comparing(Advertisement::getCreatedAt);
                break;
        }

        if ("asc".equalsIgnoreCase(sortOrder)) {
            return ads.stream().sorted(comparator).collect(Collectors.toList());
        } else {
            return ads.stream().sorted(comparator.reversed()).collect(Collectors.toList());
        }
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
     * Restore an advertisement (after deleting)
     */
    @Transactional
    public Advertisement restoreAd (Long adId){
        Advertisement ad = findById(adId);
        ad.setStatus(AdStatus.PENDING);
        ad.setUpdatedAt(LocalDateTime.now());

        return adRepository.save(ad);
    }

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
     * Delete advertisement (admin only - soft delete)
     */
    @Transactional
    public void deleteAd(Long adId) {
        Advertisement ad = findById(adId);
        ad.setStatus(AdStatus.DELETED);
        ad.setUpdatedAt(LocalDateTime.now());
    }

    // OWNER METHODS

    /**
     * Soft delete advertisement (owner)
     */
    @Transactional
    public Advertisement deleteAd(Advertisement ad, Long ownerId) {
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
    public Advertisement markAsSold(Advertisement ad, Long ownerId) {
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
    public Advertisement updateRating(Advertisement ad, int newRating) {

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

    // CHECK METHOD

    public void checkAdDeleted (Advertisement ad){
        if(ad.isDeleted()) throw new InvalidOperationException("The operation failed: The advertisement is deleted");
    }
    public void checkAdPending (Advertisement ad){
        if(ad.isPending()) throw new InvalidOperationException("The operation failed: The advertisement hasn't been approved yet");
    }
    public void checkAdRejected (Advertisement ad){
        if(ad.isRejected()) throw new InvalidOperationException("The operation failed: The advertisement is rejected");
    }
    public void checkAdSold (Advertisement ad){
        if(ad.isSold()) throw new InvalidOperationException("The operation failed: The advertisement is sold");
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
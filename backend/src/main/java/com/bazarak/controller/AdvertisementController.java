package com.bazarak.controller;

import com.bazarak.entity.*;
import com.bazarak.service.*;
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
@RequestMapping("/ads")
public class AdvertisementController {

    @Autowired
    private AdvertisementService advertisementService;
    @Autowired
    private FavoriteService favoriteService;
    @Autowired
    private UserService userService;
    @Autowired
    private CityService cityService;
    @Autowired
    private CategoryService categoryService;

    // GET ALL ACTIVE ADS (Public)

    /**
     * Get all active advertisements (home page)
     */
    @GetMapping("/active")
    public ResponseEntity<?> getAllActiveAds() {
        List<Advertisement> ads = advertisementService.getActiveAdsWithDetails();
        return ResponseEntity.ok(ads);
    }

    // GET AD BY ID (Public)

    /**
     * Get advertisement details by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getAdById(@PathVariable Long id, HttpSession session) {
        try {
            Advertisement ad = advertisementService.findById(id);

            // Check if current user has favorited this ad
            boolean isFavorited = false;
            User currentUser = userService.getCurrentUserOrThrow(session);
            if (currentUser != null) {
                isFavorited = favoriteService.isFavorited(currentUser, ad);
            }

            // Add favorite status to response
            Map<String, Object> response = new HashMap<>();
            response.put("ad", ad);
            response.put("isFavorited", isFavorited);
            response.put("favoriteCount", ad.getFavoriteCount() != null ? ad.getFavoriteCount() : 0);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // CREATE NEW AD (Authenticated)

    /**
     * Create a new advertisement
     */
    @PostMapping
    public ResponseEntity<?> createAd(@Valid @RequestBody AdRequest request, HttpSession session) {
        // 1. Check if user is logged in
        User currentUser = userService.getCurrentUserOrThrow(session);

        // 2. Check if user is blocked
        if (!currentUser.isActive()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Your account is blocked. You cannot post ads.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        try {
            // 3. Create the ad
            Advertisement ad = new Advertisement();
            ad.setTitle(request.getTitle());
            ad.setDescription(request.getDescription());
            ad.setPrice(request.getPrice());

            //TODO: add addImage method
            Advertisement createdAd = advertisementService.createAd(ad, currentUser.getId(), request.getCityId(),request.getCategoryId());

            // 4. Return response
            Map<String, Object> response = new HashMap<>();
            response.put("id", createdAd.getId());
            response.put("title", createdAd.getTitle());
            response.put("status", createdAd.getStatus());
            response.put("message", "Ad created successfully. Waiting for admin approval.");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // UPDATE AD (Owner only)

    /**
     * Update an existing advertisement
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAd(@PathVariable Long id,
                                      @Valid @RequestBody AdRequest request,
                                      HttpSession session) {
        // 1. Check if user is logged in
        User currentUser = userService.getCurrentUserOrThrow(session);
        try {
            // 2. Get existing ad
            Advertisement existingAd = advertisementService.findById(id);
            advertisementService.checkAdDeleted(existingAd);
            advertisementService.checkAdRejected(existingAd);
            advertisementService.checkAdSold(existingAd);

            // 3. Check ownership
            if (!existingAd.getOwner().getId().equals(currentUser.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "You don't own this advertisement");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // 4. Update fields
            Advertisement updatedAd = new Advertisement();
            City city = cityService.getCityById(request.getCityId());
            Category category = categoryService.getCategoryById(request.getCategoryId());

            //UPDATING FIELDS
            updatedAd.setTitle(request.getTitle());
            updatedAd.setDescription(request.getDescription());
            updatedAd.setPrice(request.getPrice());
            updatedAd.setCity(city);
            updatedAd.setCategory(category);

            //TODO: add addImage method
            Advertisement savedAd = advertisementService.updateAd(id, updatedAd, currentUser.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("id", savedAd.getId());
            response.put("title", savedAd.getTitle());
            response.put("status", savedAd.getStatus());
            response.put("message", "Ad updated successfully");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // DELETE AD (Owner only)

    /**
     * Delete an advertisement (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAd(@PathVariable Long id, HttpSession session) {
        // 1. Check if user is logged in
        User currentUser = userService.getCurrentUserOrThrow(session);

        try {
            Advertisement ad = advertisementService.findById(id);
            advertisementService.checkAdDeleted(ad);

            // 2. Check ownership and delete
            advertisementService.deleteAd(ad, currentUser.getId());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Ad deleted successfully");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // MARK AD AS SOLD (Owner only)

    /**
     * Mark an advertisement as sold
     */
    @PutMapping("/{id}/sold")
    public ResponseEntity<?> markAsSold(@PathVariable Long id, HttpSession session) {
        // 1. Check if user is logged in
        User currentUser = userService.getCurrentUserOrThrow(session);

        try {
            Advertisement advertisement = advertisementService.findById(id);
            advertisementService.checkAdRejected(advertisement);
            advertisementService.checkAdDeleted(advertisement);
            advertisementService.checkAdPending(advertisement);
            Advertisement result = advertisementService.markAsSold(advertisement, currentUser.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("title", result.getTitle());
            response.put("status", result.getStatus());
            response.put("ad", result);
            response.put("message", "Ad marked as sold successfully");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // SEARCH ADS WITH SORTING (Public)
    /**
     * Search advertisements by keyword and filters with sorting options
     *
     * Sort Options:
     * - sortBy: createdAt (default), price, title
     * - sortOrder: desc (default), asc
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchAds(@RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) Long categoryId,
                                       @RequestParam(required = false) Long cityId,
                                       @RequestParam(required = false) Long minPrice,
                                       @RequestParam(required = false) Long maxPrice,
                                       @RequestParam(required = false, defaultValue = "created_at") String sortBy,
                                       @RequestParam(required = false, defaultValue = "desc") String sortOrder) {
        try {
            // 1 Get results from database
            List<Advertisement> results = advertisementService.searchAdsWithFilters(
                    keyword, categoryId, cityId, minPrice, maxPrice);

            // 2 Apply sorting (using the reusable method!)
            results = advertisementService.applySorting(results, sortBy, sortOrder);

            Map<String, Object> response = new HashMap<>();
            response.put("count", results.size());
            response.put("sortBy", sortBy);
            response.put("sortOrder", sortOrder);
            response.put("results", results);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // GET ADS BY OWNER (Public)

    /**
     * Get all active ads by a specific user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getAdsByUser(@PathVariable Long userId) {
        try {
            List<Advertisement> activeAds = advertisementService.getActiveAdsByOwner(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("count", activeAds.size());
            response.put("ads", activeAds);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // HELPER METHOD

    private ResponseEntity<?> buildErrorResponse(HttpStatus status, String message){
        Map<String, String> error = new HashMap<>();
        error.put("error",message);
        error.put("status", String.valueOf(status.value()));
        return ResponseEntity.status(status).body(error);
    }

    // REQUEST DTO CLASSES (Inner classes)

    /**
     * Create Ad Request DTO (Data Transfer Object)
     */
    public static class AdRequest {
        private String title;
        private String description;
        private Long price;
        private Long categoryId;
        private Long cityId;

        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Long getPrice() { return price; }
        public void setPrice(Long price) { this.price = price; }
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        public Long getCityId() { return cityId; }
        public void setCityId(Long cityId) { this.cityId = cityId; }
    }

}
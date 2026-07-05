package com.bazarak.controller;

import com.bazarak.entity.Advertisement;
import com.bazarak.entity.User;
import com.bazarak.service.AdvertisementService;
import com.bazarak.service.UserService;
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
    private UserService userService;

    // GET ALL ACTIVE ADS (Public)

    /**
     * Get all active advertisements (home page)
     */
    @GetMapping
    public ResponseEntity<?> getAllActiveAds() {
        List<Advertisement> ads = advertisementService.getActiveAdsWithDetails();
        return ResponseEntity.ok(ads);
    }

    // GET AD BY ID (Public)

    /**
     * Get advertisement details by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getAdById(@PathVariable Long id) {
        try {
            Advertisement ad = advertisementService.findById(id);
            return ResponseEntity.ok(ad);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // CREATE NEW AD (Authenticated)

    /**
     * Create a new advertisement
     */
    @PostMapping
    public ResponseEntity<?> createAd(@Valid @RequestBody CreateAdRequest request, HttpSession session) {
        // 1. Check if user is logged in
        User currentUser = userService.getCurrentUserOrThrow(session);
        if (currentUser == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Please login to post an ad");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

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

            // Set category and city (we'll need to fetch them from database)
            // For now, we'll just set the IDs

            Advertisement createdAd = advertisementService.createAd(ad, currentUser.getId());

            // 4. Return response
            Map<String, Object> response = new HashMap<>();
            response.put("id", createdAd.getId());
            response.put("title", createdAd.getTitle());
            response.put("status", createdAd.getStatus());
            response.put("message", "Ad created successfully. Waiting for admin approval.");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // UPDATE AD (Owner only)

    /**
     * Update an existing advertisement
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAd(@PathVariable Long id,
                                      @Valid @RequestBody UpdateAdRequest request,
                                      HttpSession session) {
        // 1. Check if user is logged in
        User currentUser = userService.getCurrentUserOrThrow(session);
        if (currentUser == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Please login to update an ad");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        try {
            // 2. Get existing ad
            Advertisement existingAd = advertisementService.findById(id);

            // 3. Check ownership
            if (!existingAd.getOwner().getId().equals(currentUser.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "You don't own this advertisement");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // 4. Update fields
            Advertisement updatedAd = new Advertisement();
            updatedAd.setTitle(request.getTitle());
            updatedAd.setDescription(request.getDescription());
            updatedAd.setPrice(request.getPrice());

            Advertisement savedAd = advertisementService.updateAd(id, updatedAd, currentUser.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("id", savedAd.getId());
            response.put("title", savedAd.getTitle());
            response.put("status", savedAd.getStatus());
            response.put("message", "Ad updated successfully");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
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
        if (currentUser == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Please login to delete an ad");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        try {
            // 2. Check ownership and delete
            advertisementService.deleteAdUser(id, currentUser.getId());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Ad deleted successfully");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
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
        if (currentUser == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Please login to mark ad as sold");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        try {
            Advertisement ad = advertisementService.markAsSold(id, currentUser.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("id", ad.getId());
            response.put("title", ad.getTitle());
            response.put("status", ad.getStatus());
            response.put("message", "Ad marked as sold successfully");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // SEARCH ADS (Public)

    /**
     * Search advertisements by keyword and filters
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchAds(@RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) Long categoryId,
                                       @RequestParam(required = false) Long cityId,
                                       @RequestParam(required = false) Long minPrice,
                                       @RequestParam(required = false) Long maxPrice) {
        try {
            List<Advertisement> results = advertisementService.searchAdsWithFilters(
                    keyword, categoryId, cityId, minPrice, maxPrice);

            Map<String, Object> response = new HashMap<>();
            response.put("count", results.size());
            response.put("results", results);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // GET ADS BY OWNER (Public)

    /**
     * Get all active ads by a specific user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getAdsByUser(@PathVariable Long userId) {
        try {
            List<Advertisement> ads = advertisementService.getAdsByOwner(userId);
            // Only return active ads
            List<Advertisement> activeAds = ads.stream()
                    .filter(ad -> ad.getStatus() == Advertisement.AdStatus.ACCEPTED)
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("count", activeAds.size());
            response.put("ads", activeAds);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // REQUEST DTO CLASSES (Inner classes)

    /**
     * Create Ad Request DTO (Data Transfer Object)
     */
    public static class CreateAdRequest {
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

    /**
     * Update Ad Request DTO
     */
    public static class UpdateAdRequest {
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
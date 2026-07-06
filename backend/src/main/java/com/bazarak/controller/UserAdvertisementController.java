package com.bazarak.controller;

import com.bazarak.entity.Advertisement;
import com.bazarak.entity.User;
import com.bazarak.service.AdvertisementService;
import com.bazarak.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users/me")
public class UserAdvertisementController {
    @Autowired
    private AdvertisementService advertisementService;
    @Autowired
    private UserService userService;

    // 1. GET ALL MY ADS (with sorting)

    /**
     * Get all advertisements posted by the current user
     */
    @GetMapping("/ads")
    public ResponseEntity<?> getMyAds(@RequestParam(required = false, defaultValue = "created_at") String sortBy,
                                      @RequestParam(required = false, defaultValue = "desc") String sortOrder,
                                      HttpSession session) {
        User currentUser = userService.getCurrentUserOrThrow(session);

        try {
            // 1 Get all my ads
            List<Advertisement> myAds = advertisementService.getAdsByOwner(currentUser.getId());

            // 2 Apply sorting
            myAds = advertisementService.applySorting(myAds, sortBy, sortOrder);

            Map<String, Object> response = new HashMap<>();
            response.put("count", myAds.size());
            response.put("sortBy", sortBy);
            response.put("sortOrder", sortOrder);
            response.put("ads", myAds);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // 2. GET MY ADS BY STATUS (with sorting)

    /**
     * Get the current user's ads filtered by status
     */
    @GetMapping("/ads/status/{status}")
    public ResponseEntity<?> getMyAdsByStatus(@PathVariable String status,
                                              @RequestParam(required = false, defaultValue = "created_at") String sortBy,
                                              @RequestParam(required = false, defaultValue = "desc") String sortOrder,
                                              HttpSession session) {
        // Check if user is logged in
        User currentUser = userService.getCurrentUserOrThrow(session);

        try {
            // Validate status
            Advertisement.AdStatus adStatus;
            try {
                adStatus = Advertisement.AdStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid status. Valid values: PENDING, ACTIVE, REJECTED, SOLD, DELETED");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            List<Advertisement> myAds = advertisementService.getAdsByStatus(adStatus);

            // Filter to only current user's ads
            List<Advertisement> filteredAds = myAds.stream()
                    .filter(ad -> ad.getOwner().getId().equals(currentUser.getId()))
                    .collect(Collectors.toList());

            filteredAds = advertisementService.applySorting(filteredAds, sortBy, sortOrder);

            Map<String, Object> response = new HashMap<>();
            response.put("status", adStatus);
            response.put("count", filteredAds.size());
            response.put("sortBy", sortBy);
            response.put("sortOrder", sortOrder);
            response.put("ads", filteredAds);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 3. GET MY ADS GROUPED BY STATUS (Dashboard)

    /**
     * Get the current user's ads grouped by status
     * Useful for dashboard/overview
     */
    @GetMapping("/ads/dashboard")
    public ResponseEntity<?> getMyAdsDashboard(HttpSession session) {
        // Check if user is logged in
        User currentUser = userService.getCurrentUserOrThrow(session);

        try {
            List<Advertisement> myAds = advertisementService.getAdsByOwner(currentUser.getId());

            // Group by status
            List<Advertisement> pendingAds = myAds.stream()
                    .filter(ad -> ad.getStatus() == Advertisement.AdStatus.PENDING)
                    .collect(Collectors.toList());

            List<Advertisement> activeAds = myAds.stream()
                    .filter(ad -> ad.getStatus() == Advertisement.AdStatus.ACCEPTED)
                    .collect(Collectors.toList());

            List<Advertisement> rejectedAds = myAds.stream()
                    .filter(ad -> ad.getStatus() == Advertisement.AdStatus.REJECTED)
                    .collect(Collectors.toList());

            List<Advertisement> soldAds = myAds.stream()
                    .filter(ad -> ad.getStatus() == Advertisement.AdStatus.SOLD)
                    .collect(Collectors.toList());

            List<Advertisement> deletedAds = myAds.stream()
                    .filter(ad -> ad.getStatus() == Advertisement.AdStatus.DELETED)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("total", myAds.size());
            response.put("pending", Map.of("count", pendingAds.size(), "ads", pendingAds));
            response.put("active", Map.of("count", activeAds.size(), "ads", activeAds));
            response.put("rejected", Map.of("count", rejectedAds.size(), "ads", rejectedAds));
            response.put("sold", Map.of("count", soldAds.size(), "ads", soldAds));
            response.put("deleted", Map.of("count", deletedAds.size(), "ads", deletedAds));

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 4. GET SPECIFIC AD

    /**
     * Get a specific ad (user must own it)
     */
    @GetMapping("/ads/{adId}")
    public ResponseEntity<?> getMySpecificAd(@PathVariable Long adId, HttpSession session) {
        User user = userService.getCurrentUserOrThrow(session);

        try {
            Advertisement ad = advertisementService.findById(adId);
            userService.checkOwnership(user.getId(), session);
            return ResponseEntity.ok(ad);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // 5. GET MY RECENT ADS

    /**
     * Get the current user's most recent ads
     */
    @GetMapping("/ads/recent")
    public ResponseEntity<?> getMyRecentAds(HttpSession session) {
        // Check if user is logged in
        User user = userService.getCurrentUserOrThrow(session);
        try {
            List<Advertisement> myAds = advertisementService.getAdsByOwner(user.getId());
            // Sort by createdAt descending (newest first)
            List<Advertisement> recentAds = myAds.stream()
                    .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("count", recentAds.size());
            response.put("ads", recentAds);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 6. GET MY ACTIVE ADS (with sorting)

    /**
     * Get only active ads posted by the current user
     */
    @GetMapping("/ads/active")
    public ResponseEntity<?> getMyActiveAds(@RequestParam(required = false, defaultValue = "created_at") String sortBy,
                                            @RequestParam(required = false, defaultValue = "desc") String sortOrder,
                                            HttpSession session) {
        // Check if user is logged in
        User currentUser = userService.getCurrentUserOrThrow(session);
        try {
            // Get the users active ads
            List<Advertisement> myAds = advertisementService.getAdsByOwner(currentUser.getId());
            List<Advertisement> activeAds = myAds.stream().
                    filter(ad -> ad.getStatus() == Advertisement.AdStatus.ACCEPTED).
                    collect(Collectors.toList());

            // Apply sorting
            activeAds = advertisementService.applySorting(activeAds, sortBy, sortOrder);

            Map<String, Object> response = new HashMap<>();
            response.put("count", activeAds.size());
            response.put("sortBy", sortBy);
            response.put("sortOrder", sortOrder);
            response.put("ads", activeAds);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 7. GET MY PENDING ADS ONLY (with sorting)

    /**
     * Get only pending ads posted by the current user
     */
    @GetMapping("/ads/pending")
    public ResponseEntity<?> getMyPendingAds(@RequestParam(required = false, defaultValue = "created_at") String sortBy,
                                             @RequestParam(required = false, defaultValue = "desc") String sortOrder,
                                             HttpSession session) {
        // Check if user is logged in
        User user = userService.getCurrentUserOrThrow(session);

        try {
            List<Advertisement> myAds = advertisementService.getAdsByOwner(user.getId());
            List<Advertisement> pendingAds = myAds.stream().
                    filter(ad -> ad.getStatus() == Advertisement.AdStatus.PENDING).
                    collect(Collectors.toList());

            pendingAds = advertisementService.applySorting(pendingAds, sortBy, sortOrder);

            Map<String, Object> response = new HashMap<>();
            response.put("count", pendingAds.size());
            response.put("sortBy", sortBy);
            response.put("sortOrder", sortOrder);
            response.put("ads", pendingAds);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 8. GET MY REJECTED ADS ONLY (with sorting)

    /**
     * Get rejected ads posted by the current user (with rejection reason)
     */
    @GetMapping("/ads/rejected")
    public ResponseEntity<?> getMyRejectedAds(@RequestParam(required = false, defaultValue = "created_at") String sortBy,
                                              @RequestParam(required = false, defaultValue = "desc") String sortOrder,
                                              HttpSession session) {
        // Check if user is logged in
        User currentUser = userService.getCurrentUserOrThrow(session);

        try {
            List<Advertisement> myAds = advertisementService.getAdsByOwner(currentUser.getId());
            List<Advertisement> rejectedAds = myAds.stream().
                    filter(ad -> ad.getStatus() == Advertisement.AdStatus.REJECTED).
                    collect(Collectors.toList());

            rejectedAds = advertisementService.applySorting(rejectedAds, sortBy, sortOrder);

            Map<String, Object> response = new HashMap<>();
            response.put("count", rejectedAds.size());
            response.put("sortBy", sortBy);
            response.put("sortOrder", sortOrder);
            response.put("ads", rejectedAds);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 9. GET MY SOLD ADS ONLY (with sorting)

    /**
     * Get sold ads posted by the current user
     */
    @GetMapping("/ads/sold")
    public ResponseEntity<?> getMySoldAds(@RequestParam(required = false, defaultValue = "created_at") String sortBy,
                                          @RequestParam(required = false, defaultValue = "desc") String sortOrder,
                                          HttpSession session) {
        // Check if user is logged in
        User currentUser = userService.getCurrentUserOrThrow(session);
        try {
            List<Advertisement> myAds = advertisementService.getAdsByOwner(currentUser.getId());
            List<Advertisement> soldAds = myAds.stream().
                    filter(ad -> ad.getStatus() == Advertisement.AdStatus.SOLD).
                    collect(Collectors.toList());

            soldAds = advertisementService.applySorting(soldAds, sortBy, sortOrder);

            Map<String, Object> response = new HashMap<>();
            response.put("count", soldAds.size());
            response.put("sortBy", sortBy);
            response.put("sortOrder", sortOrder);
            response.put("ads", soldAds);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 10. GET MY DELETED ADS ONLY (with sorting)
    @GetMapping("/ads/deleted")
    public ResponseEntity<?> getMyDeletedAds(@RequestParam(required = false, defaultValue = "created_at") String sortBy,
                                             @RequestParam(required = false, defaultValue = "desc") String sortOrder,
                                             HttpSession session) {
        // Check if user is logged in
        User currentUser = userService.getCurrentUserOrThrow(session);

        try {
            List<Advertisement> myAds = advertisementService.getAdsByOwner(currentUser.getId());
            List<Advertisement> deletedAds = myAds.stream().
                    filter(ad -> ad.getStatus() == Advertisement.AdStatus.DELETED).
                    collect(Collectors.toList());

            deletedAds = advertisementService.applySorting(deletedAds, sortBy, sortOrder);

            Map<String, Object> response = new HashMap<>();
            response.put("count", deletedAds.size());
            response.put("sortBy", sortBy);
            response.put("sortOrder", sortOrder);
            response.put("ads", deletedAds);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // HELPER METHOD
    public ResponseEntity<?> buildErrorResponse(HttpStatus status, String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        error.put("status", String.valueOf(status.value()));
        return ResponseEntity.status(status).body(error);
    }
}

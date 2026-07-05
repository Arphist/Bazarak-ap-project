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
@RequestMapping("/user/ads")
public class UserAdvertisementController {
    @Autowired
    private AdvertisementService advertisementService;
    @Autowired
    private UserService userService;

    // 1. GET ALL MY ADS

    /**
     * Get all advertisements posted by the current user
     */
    @GetMapping("/ads")
    public ResponseEntity<?> getMyAds(HttpSession session) {
        // Check if user is logged in
        User currentUser = userService.getCurrentUserOrThrow(session);

        try {
            List<Advertisement> myAds = advertisementService.getAdsByOwner(currentUser.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("count", myAds.size());
            response.put("ads", myAds);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 2. GET MY ADS BY STATUS
    /**
     * Get the current user's ads filtered by status
     */
    @GetMapping("/ads/status/{status}")
    public ResponseEntity<?> getMyAdsByStatus(@PathVariable String status, HttpSession session) {
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

            Map<String, Object> response = new HashMap<>();
            response.put("status", adStatus);
            response.put("count", filteredAds.size());
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
    public ResponseEntity<?> getMyRecentAds(@RequestParam(defaultValue = "5") int limit, HttpSession session) {
        // Check if user is logged in
        User user = userService.getCurrentUserOrThrow(session);
        try{
            List<Advertisement> myAds = advertisementService.getAdsByOwner(user.getId());
            // Sort by createdAt descending (newest first)
            List<Advertisement> recentAds = myAds.stream()
                    .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))
                    .limit(limit)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("limit",limit);
            response.put("count",recentAds.size());
            response.put("ads",recentAds);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 6. GET MY ACTIVE ADS ONLY
    /**
     * Get only active ads posted by the current user
     */
    @GetMapping("/ads/active")
    public ResponseEntity<?> getMyActiveAds (HttpSession session){
        // Check if user is logged in
        User currentUser = userService.getCurrentUserOrThrow(session);
        try{
            // Get the users active ads
            List<Advertisement> myAds = advertisementService.getAdsByOwner(currentUser.getId());
            List<Advertisement> activeAds = myAds.stream().
                    filter(ad -> ad.getStatus()== Advertisement.AdStatus.ACCEPTED).
                    collect(Collectors.toList());;
            Map<String, Object> response = new HashMap<>();
            response.put("count", activeAds.size());
            response.put("ads", activeAds);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

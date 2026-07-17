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

@RestController
@RequestMapping("/admin/ads")
public class AdminAdvertisementController {

    @Autowired
    private AdvertisementService advertisementService;

    @Autowired
    private UserService userService;

    // HELPER METHOD: Check Admin Access

    // 1. GET ALL PENDING ADS (with sorting)

    /**
     * Get all advertisements waiting for admin approval
     */
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingAds(@RequestParam(required = false, defaultValue = "created_at") String sortBy,
                                           @RequestParam(required = false, defaultValue = "asc") String sortOrder,
                                           HttpSession session) {
        // Check if user is logged in
        User currentUser = userService.getCurrentUserOrThrow(session);

        userService.checkAdmin(session);

        try {
            List<Advertisement> pendingAds = advertisementService.getPendingAds();
            // sorting
            pendingAds = advertisementService.applySorting(pendingAds, sortBy, sortOrder);
            Map<String, Object> response = new HashMap<>();
            response.put("count", pendingAds.size());
            response.put("sortBy", sortBy);
            response.put("sortOrder", sortOrder);
            response.put("ads", pendingAds);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // 2. APPROVE AD

    /**
     * Approve a pending advertisement
     */
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveAd(@PathVariable Long id, HttpSession session) {
        // Check if user is logged in
        User currentUser = userService.getCurrentUserOrThrow(session);

        userService.checkAdmin(session);

        try {
            Advertisement approvedAd = advertisementService.approveAd(id);

            Map<String, Object> response = new HashMap<>();
            response.put("ad", approvedAd);
            response.put("title", approvedAd.getTitle());
            response.put("status", approvedAd.getStatus());
            response.put("message", "Ad approved successfully");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // 3. REJECT AD

    /**
     * Reject a pending advertisement (with optional reason)
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectAd(@PathVariable Long id,
                                      @RequestBody(required = false) RejectRequest rejectRequest,
                                      HttpSession session) {

        userService.checkAdmin(session);

        try {
            String reason = rejectRequest != null ? rejectRequest.getReason() : "No reason provided";

            if (reason.length() > 100) {
                reason = reason.substring(0, 100);
            }

            Advertisement rejectedAd = advertisementService.rejectAd(id, reason);

            Map<String, Object> response = new HashMap<>();
            response.put("ad", rejectedAd);
            response.put("title", rejectedAd.getTitle());
            response.put("status", rejectedAd.getStatus());
            response.put("reason", reason);
            response.put("message", "Ad rejected successfully");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // 4. DELETE ANY AD (Hard Delete)

    /**
     * Delete any advertisement (hard delete - admin only)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAd(@PathVariable Long id, HttpSession session) {
        // Check if user is logged in
        User currentUser = userService.getCurrentUserOrThrow(session);

        userService.checkAdmin(session);

        try {
            advertisementService.deleteAd(id);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Ad deleted successfully");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // 5. GET ADS BY STATUS (Admin Only) (with sorting)

    /**
     * Get all ads by status (for admin dashboard)
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getAdsByStatus(@PathVariable String status,
                                            @RequestParam(required = false, defaultValue = "created_at") String sortBy,
                                            @RequestParam(required = false, defaultValue = "desc") String sortOrder,
                                            HttpSession session) {

        userService.checkAdmin(session);

        try {
            Advertisement.AdStatus adStatus;
            try {
                adStatus = Advertisement.AdStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid status. Valid values: PENDING, ACTIVE, REJECTED, SOLD, DELETED");
            }

            List<Advertisement> ads = advertisementService.getAdsByStatus(adStatus);
            ads=advertisementService.applySorting(ads,sortBy,sortOrder);

            Map<String, Object> response = new HashMap<>();
            response.put("status", adStatus);
            response.put("count", ads.size());
            response.put("sortBy", sortBy);
            response.put("sortOrder", sortOrder);
            response.put("ads", ads);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // 6. GET ADMIN DASHBOARD STATISTICS

    /**
     * Get admin dashboard statistics
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStats(HttpSession session) {
        // Check if user is logged in
        User currentUser = userService.getCurrentUserOrThrow(session);

        userService.checkAdmin(session);

        try {
            Map<String, Object> stats = new HashMap<>();

            // Get counts
            long pendingCount = advertisementService.getPendingAdCount();
            long activeCount = advertisementService.getActiveAdCount();
            long totalAds = advertisementService.findAllAds().size();

            stats.put("totalAds", totalAds);
            stats.put("pendingAds", pendingCount);
            stats.put("activeAds", activeCount);

            // Get category stats
            List<Object[]> categoryStats = advertisementService.getAdsByCategoryStats();
            stats.put("adsByCategory", categoryStats);

            // Get city stats
            List<Object[]> cityStats = advertisementService.getAdsByCityStats();
            stats.put("adsByCity", cityStats);

            // Get recent ads
            List<Advertisement> recentAds = advertisementService.getRecentAds(7); // Last 7 days
            stats.put("recentAds", recentAds.size());

            return ResponseEntity.ok(stats);

        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // 7. GET ALL ADS (Admin Only) (with sorting)

    /**
     * Get all advertisements (including non-active ones)
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllAds(@RequestParam(required = false, defaultValue = "created_at") String sortBy,
                                       @RequestParam(required = false, defaultValue = "desc") String sortOrder,
                                       HttpSession session) {

        // Check if user is logged in
        User currentUser = userService.getCurrentUserOrThrow(session);

        userService.checkAdmin(session);

        try {
            List<Advertisement> allAds = advertisementService.findAllAds();
            allAds = advertisementService.applySorting(allAds, sortBy, sortOrder);

            Map<String, Object> response = new HashMap<>();
            response.put("count", allAds.size());
            response.put("sortBy", sortBy);
            response.put("sortOrder", sortOrder);
            response.put("ads", allAds);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // HELPER METHOD
    private ResponseEntity<?> buildErrorResponse(HttpStatus status, String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        error.put("status", String.valueOf(status.value()));
        return ResponseEntity.status(status).body(error);
    }

    // INNER CLASSES (DTOs)

    /**
     * Reject Request DTO
     */
    public static class RejectRequest {
        private String reason;

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
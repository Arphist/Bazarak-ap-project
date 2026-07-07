package com.bazarak.controller;

import com.bazarak.entity.Rating;
import com.bazarak.service.RatingService;
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
@RequestMapping("/ratings")
public class RatingController {

    @Autowired
    private RatingService ratingService;

    @Autowired
    private UserService userService;

    /**
     * Get all ratings for a seller.
     *
     * @param sellerId the seller's ID
     * @return list of ratings
     */
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<Rating>> getRatingsBySeller(@PathVariable Long sellerId) {
        return ResponseEntity.ok(ratingService.getRatingsBySeller(sellerId));
    }

    /**
     * Get average score for a seller.
     *
     * @param sellerId the seller's ID
     * @return average score
     */
    @GetMapping("/seller/{sellerId}/average")
    public ResponseEntity<Map<String, Object>> getAverageScore(@PathVariable Long sellerId) {
        Double average = ratingService.getAverageScoreForSeller(sellerId);
        Long count = ratingService.getRatingCountForSeller(sellerId);

        Map<String, Object> response = new HashMap<>();
        response.put("sellerId", sellerId);
        response.put("averageScore", average);
        response.put("totalRatings", count);

        return ResponseEntity.ok(response);
    }

    /**
     * Get all ratings given by the current user.
     *
     * @param session the HTTP session
     * @return list of ratings
     */
    @GetMapping("/my-ratings")
    public ResponseEntity<List<Rating>> getMyRatings(HttpSession session) {
        Long buyerId = userService.getCurrentUserOrThrow(session).getId();
        return ResponseEntity.ok(ratingService.getRatingsByBuyer(buyerId));
    }

    /**
     * Get all ratings for a specific advertisement.
     *
     * @param advertisementId the advertisement's ID
     * @return list of ratings
     */
    @GetMapping("/advertisement/{advertisementId}")
    public ResponseEntity<List<Rating>> getRatingsByAdvertisement(@PathVariable Long advertisementId) {
        return ResponseEntity.ok(ratingService.getRatingsByAdvertisement(advertisementId));
    }

    /**
     * Create a new rating.
     *
     * @param request the rating creation request
     * @param session the HTTP session
     * @return the created rating
     */
    @PostMapping
    public ResponseEntity<Rating> createRating(
            @Valid @RequestBody RatingRequest request,
            HttpSession session) {

        Long buyerId = userService.getCurrentUserOrThrow(session).getId();

        Rating rating = ratingService.createRating(
                request.getScore(),
                request.getComment(),
                buyerId,
                request.getSellerId(),
                request.getAdvertisementId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(rating);
    }

    /**
     * Delete a rating (admin only).
     *
     * @param id      the rating ID
     * @param session the HTTP session
     * @return success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteRating(
            @PathVariable Long id,
            HttpSession session) {

        userService.checkAdmin(session);

        ratingService.deleteRating(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Rating deleted successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * DTO for rating creation requests.
     */
    public static class RatingRequest {
        private Integer score;
        private String comment;
        private Long sellerId;
        private Long advertisementId;

        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }

        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }

        public Long getSellerId() { return sellerId; }
        public void setSellerId(Long sellerId) { this.sellerId = sellerId; }

        public Long getAdvertisementId() { return advertisementId; }
        public void setAdvertisementId(Long advertisementId) { this.advertisementId = advertisementId; }
    }
}
package com.bazarak.service;

import com.bazarak.entity.Advertisement;
import com.bazarak.entity.Rating;
import com.bazarak.entity.User;
import com.bazarak.exception.rating.DuplicateRatingException;
import com.bazarak.exception.rating.InvalidRatingValueException;
import com.bazarak.exception.rating.RatingNotFoundException;
import com.bazarak.exception.user.UserNotFoundException;
import com.bazarak.repository.RatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RatingService {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AdvertisementService advertisementService;

    /**
     * Create a new rating for a seller.
     *
     * @param score          rating score (1-5)
     * @param comment        optional comment
     * @param buyerId        ID of the buyer giving the rating
     * @param sellerId       ID of the seller receiving the rating
     * @param advertisementId ID of the advertisement
     * @return the created rating
     */
    public Rating createRating(Integer score, String comment, Long buyerId, Long sellerId, Long advertisementId) {
        // 1. Validate score
        if (score == null || score < 1 || score > 5) {
            throw new InvalidRatingValueException("Rating must be between 1 and 5");
        }

        // 2. Get buyer, seller, and advertisement
        User buyer = userService.getUserById(buyerId);
        User seller = userService.getUserById(sellerId);
        Advertisement advertisement = advertisementService.findById(advertisementId);

        // 3. Check if buyer is trying to rate themselves
        if (buyerId.equals(sellerId)) {
            throw new InvalidRatingValueException("You cannot rate yourself");
        }

        // 4. Check if buyer is blocked or seller is blocked
        if (!buyer.isActive()) {
            throw new InvalidRatingValueException("Your account is blocked. You cannot rate.");
        }

        if (!seller.isActive()) {
            throw new InvalidRatingValueException("This seller is blocked. You cannot rate them.");
        }

        // 5. Check if advertisement is active or sold
        if (!advertisement.isActive() && !advertisement.isSold()) {
            throw new InvalidRatingValueException("You can only rate active or sold advertisements");
        }

        // 6. Check if buyer already rated this seller for this advertisement
        if (ratingRepository.existsByBuyerIdAndSellerIdAndAdvertisementId(buyerId, sellerId, advertisementId)) {
            throw new DuplicateRatingException("You have already rated this seller for this advertisement");
        }

        // 7. Create and save rating
        Rating rating = new Rating(score, comment, buyer, seller, advertisement);
        Rating savedRating = ratingRepository.save(rating);

        // 8. Update advertisement's average rating
        advertisementService.updateRating(advertisementId, score);

        return savedRating;
    }

    /**
     * Get all ratings for a seller.
     *
     * @param sellerId the seller's ID
     * @return list of ratings
     */
    public List<Rating> getRatingsBySeller(Long sellerId) {
        // Check if seller exists
        userService.getUserById(sellerId);
        return ratingRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);
    }

    /**
     * Get all ratings given by a buyer.
     *
     * @param buyerId the buyer's ID
     * @return list of ratings
     */
    public List<Rating> getRatingsByBuyer(Long buyerId) {
        userService.getUserById(buyerId);
        return ratingRepository.findByBuyerId(buyerId);
    }

    /**
     * Get all ratings for an advertisement.
     *
     * @param advertisementId the advertisement's ID
     * @return list of ratings
     */
    public List<Rating> getRatingsByAdvertisement(Long advertisementId) {
        advertisementService.findById(advertisementId);
        return ratingRepository.findByAdvertisementId(advertisementId);
    }

    /**
     * Get a rating by ID.
     *
     * @param id the rating ID
     * @return the rating
     */
    public Rating getRatingById(Long id) {
        return ratingRepository.findById(id)
                .orElseThrow(() -> new RatingNotFoundException("Rating not found with id: " + id));
    }

    /**
     * Calculate average score for a seller.
     *
     * @param sellerId the seller's ID
     * @return average score (or 0.0 if no ratings)
     */
    public Double getAverageScoreForSeller(Long sellerId) {
        userService.getUserById(sellerId);
        Double avg = ratingRepository.calculateAverageScoreBySellerId(sellerId);
        return avg != null ? avg : 0.0;
    }

    /**
     * Get total rating count for a seller.
     *
     * @param sellerId the seller's ID
     * @return total number of ratings
     */
    public Long getRatingCountForSeller(Long sellerId) {
        userService.getUserById(sellerId);
        return ratingRepository.countBySellerId(sellerId);
    }

    /**
     * Delete a rating (admin only).
     *
     * @param id the rating ID
     */
    public void deleteRating(Long id) {
        Rating rating = getRatingById(id);
        ratingRepository.delete(rating);
    }

    /**
     * Delete all ratings for a seller (admin only).
     *
     * @param sellerId the seller's ID
     */
    public void deleteAllRatingsForSeller(Long sellerId) {
        userService.getUserById(sellerId);
        List<Rating> ratings = ratingRepository.findBySellerId(sellerId);
        ratingRepository.deleteAll(ratings);
    }
}
package com.controller;

import com.model.Advertisement;
import com.model.Rating;
import com.model.User;
import com.service.RatingService;
import com.util.DataHolder;
import com.util.NavigationUtil;
import com.util.SessionManager;
import com.view.ShowErrorDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;

public class RatingController {

    // FXML FIELDS

    // Rating display
    @FXML
    private Label sellerNameLabel;

    @FXML
    private Label averageRatingLabel;

    @FXML
    private Label totalRatingsLabel;

    @FXML
    private ListView<Rating> ratingsListView;

    // Create rating
    @FXML
    private ComboBox<Integer> scoreComboBox;

    @FXML
    private TextArea commentTextArea;



    @FXML
    private Button submitRatingButton;

    // DATA

    private ObservableList<Rating> ratings = FXCollections.observableArrayList();
    private Long sellerId;
    private Long advertisementId;
    // INITIALIZE

    @FXML
    private void initialize() {
        // Check if user is logged in
        if (!SessionManager.isLoggedIn()) {
            NavigationUtil.goToLogin();
            return;
        }

        // Get seller and ad IDs from DataHolder
        sellerId = DataHolder.getSelectedUserId();
        advertisementId = DataHolder.getSelectedAdId();

        if (sellerId == null) {
            ShowErrorDialog.showErrorDialog(
                    "Selection Error",
                    "No seller selected",
                    "Please select a seller first.",
                    "WARNING"
            );
            return;
        }

        // Setup score combo box (1-5)
        scoreComboBox.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        scoreComboBox.setValue(3);

        // Setup ratings list view
        ratingsListView.setItems(ratings);

        // Load data
        loadSellerInfo();
        loadRatings();
    }

    // LOAD SELLER INFO

    private void loadSellerInfo() {
        try {
            // Get seller info from ratings (use first rating to get seller)
            List<Rating> ratingList = RatingService.getRatingsBySeller(sellerId);
            if (!ratingList.isEmpty()) {
                User seller = ratingList.get(0).getSeller();
                sellerNameLabel.setText("Seller: " + seller.getFullName());
            } else {
                sellerNameLabel.setText("Seller ID: " + sellerId);
            }

            // Get average score
            Map<String, Object> avgData = RatingService.getAverageScore(sellerId);
            Double avg = (Double) avgData.getOrDefault("averageScore", 0.0);
            Long count = ((Number) avgData.getOrDefault("totalRatings", 0)).longValue();

            averageRatingLabel.setText(String.format("⭐ %.1f", avg));
            totalRatingsLabel.setText("(" + count + " ratings)");

        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Seller Info Error",
                    "Failed to load seller info",
                    e.getMessage(),
                    "ERROR"
            );
        }
    }

    // LOAD RATINGS

    private void loadRatings() {
        try {
            List<Rating> ratingList = RatingService.getRatingsBySeller(sellerId);
            ratings.clear();
            ratings.addAll(ratingList);
            ratingsListView.setItems(ratings);

            if (ratings.isEmpty()) {
                ratingsListView.setPlaceholder(new Label("No ratings yet"));
            }


        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Ratings Error",
                    "Failed to load ratings",
                    e.getMessage(),
                    "ERROR"
            );
        }
    }

    // CREATE RATING

    @FXML
    private void submitRating() {
        Integer score = scoreComboBox.getValue();
        String comment = commentTextArea.getText().trim();

        if (score == null) {
            ShowErrorDialog.showErrorDialog(
                    "Validation Error",
                    "Score not selected",
                    "Please select a score.",
                    "WARNING"
            );
            return;
        }

        try {
            // Create rating object
            Rating rating = new Rating();
            rating.setScore(score);
            rating.setComment(comment.isEmpty() ? null : comment);

            User seller = new User();
            seller.setId(sellerId);
            rating.setSeller(seller);

            Advertisement ad = new Advertisement();
            ad.setId(advertisementId);
            rating.setAdvertisement(ad);

            // Submit to backend
            Rating created = RatingService.createRating(rating);

            // Clear form
            scoreComboBox.setValue(3);
            commentTextArea.clear();
            ShowErrorDialog.showErrorDialog(
                    "Success",
                    null,
                    "Rating submitted successfully.",
                    "INFORMATION"
            );
            // Reload ratings
            loadRatings();
            loadSellerInfo();

        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Rating Error",
                    "Failed to submit rating",
                    e.getMessage(),
                    "ERROR"
            );
        }
    }

    // NAVIGATION

    @FXML
    private void goBack() {
        NavigationUtil.goBack();
    }

    @FXML
    private void goToHome() {
        NavigationUtil.goToHome();
    }



}
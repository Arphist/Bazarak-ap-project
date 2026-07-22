package com.controller;

import com.model.Rating;
import com.service.RatingService;
import com.util.NavigationUtil;
import com.view.ShowErrorDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.util.List;

public class MyRatingsController {

    // ============================================
    // FXML FIELDS
    // ============================================

    @FXML
    private ListView<Rating> ratingsListView;

    @FXML
    private Label countLabel;

    @FXML
    private Label errorLabel;

    // ============================================
    // DATA
    // ============================================

    private ObservableList<Rating> ratings = FXCollections.observableArrayList();

    // ============================================
    // INITIALIZE
    // ============================================

    @FXML
    private void initialize() {
        // Setup list view with custom cell
        ratingsListView.setItems(ratings);
        ratingsListView.setCellFactory(lv -> new RatingCell());

        if (ratings.isEmpty()) {
            ratingsListView.setPlaceholder(new Label("No ratings found"));
        } else {
            ratingsListView.setPlaceholder(null);
        }

        // Load ratings
        loadMyRatings();
    }

    // ============================================
    // LOAD MY RATINGS
    // ============================================

    private void loadMyRatings() {
        try {
            List<Rating> ratingList = RatingService.getMyRatings();
            ratings.clear();
            ratings.addAll(ratingList);
            ratingsListView.setItems(ratings);

            countLabel.setText("Total: " + ratings.size());



            errorLabel.setText("");

        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Rating Error",
                    "Failed to load your ratings",
                    e.getMessage(),
                    "ERROR"
            );
        }
    }

    // ============================================
    // NAVIGATION
    // ============================================

    @FXML
    private void goBack() {
        NavigationUtil.goBack();
    }

    @FXML
    private void goToHome() {
        NavigationUtil.goToHome();
    }

    // ============================================
    // INNER CLASS: RatingCell
    // ============================================

    private class RatingCell extends ListCell<Rating> {

        @Override
        protected void updateItem(Rating rating, boolean empty) {
            super.updateItem(rating, empty);

            if (empty || rating == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            String sellerName = rating.getSeller() != null ?
                    rating.getSeller().getUsername() : "Unknown";

            String adTitle = rating.getAdvertisement() != null ?
                    rating.getAdvertisement().getTitle() : "Unknown Ad";

            String comment = rating.getComment() != null && !rating.getComment().isEmpty() ?
                    " - " + rating.getComment() : "";

            String stars = "⭐".repeat(Math.max(0, rating.getScore()));

            String displayText = String.format(
                    "%s %d/5 - %s (Ad: %s)%s",
                    stars,
                    rating.getScore(),
                    sellerName,
                    adTitle,
                    comment
            );

            setText(displayText);
            setStyle("-fx-padding: 8 12; -fx-border-color: #333333; -fx-border-width: 0 0 1 0;");
        }
    }


}
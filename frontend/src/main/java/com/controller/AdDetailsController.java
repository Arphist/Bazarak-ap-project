package com.controller;

import com.model.Advertisement;
import com.model.AdvertisementSpecification;
import com.model.User;
import com.service.AdService;
import com.service.ConversationService;
import com.util.DataHolder;
import com.util.NavigationUtil;
import com.util.SessionManager;
import com.view.ShowErrorDialog;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import com.service.FavoriteService;
import javafx.scene.control.Button;

import java.util.Map;

public class AdDetailsController {

    // FXML FIELDS

    @FXML
    private Label titleLabel;

    @FXML
    private Label priceLabel;

    @FXML
    private VBox specificationsContainer;

    @FXML
    private Label categoryLabel;

    @FXML
    private Label cityLabel;

    @FXML
    private Label ownerLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private ImageView imageView;

    @FXML
    private VBox imageContainer;

    private Long adId;

    // Favorite UI elements
    @FXML
    private Button favoriteButton;
    @FXML
    private Label favoriteCountLabel;
    private boolean isFavorited = false;
    private Long favoriteCount = 0L;

    // INITIALIZE

    @FXML
    private void initialize() {
        // Get ad ID from DataHolder
        adId = DataHolder.getSelectedAdId();

        if (adId == null) {
            ShowErrorDialog.showErrorDialog("Failed", "No ad selected", "Please select an ad", "ERROR");
            return;
        }

        // Load ad details
        loadAdDetails(adId);

        DataHolder.clearSelectedAdId();
    }

    private void loadFavoriteState(Long adId) {
        try {
            // Only if user is logged in
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser != null) {
                isFavorited = FavoriteService.isFavorited(adId);
                updateFavoriteButton();
            }

            // Load favorite count
            favoriteCount = FavoriteService.getFavoriteCount(adId);
            favoriteCountLabel.setText(String.valueOf(favoriteCount));

        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Favorite Status Error",
                    "Unable to load favorite information",
                    "There was a problem checking if this ad is in your favorites. Please try again later.",
                    "ERROR"
            );
        }
    }

    @FXML
    private void toggleFavorite() {
        try {
            if (isFavorited) {
                // Remove from favorites
                FavoriteService.removeFavorite(adId);
                isFavorited = false;
                favoriteCount--;
            } else {
                // Add to favorites
                FavoriteService.addToFavorite(adId);
                isFavorited = true;
                favoriteCount++;
            }

            // Update UI
            updateFavoriteButton();
            favoriteCountLabel.setText(String.valueOf(favoriteCount));

        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Update Favorite Error",
                    "Failed to update favorite",
                    "There was a problem updating favorite. Please try again later.",
                    "ERROR"
            );
        }
    }

    private void updateFavoriteButton() {
        if (isFavorited) {
            favoriteButton.setText("♥");  // Filled heart
            favoriteButton.setStyle("-fx-font-size: 28px; -fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-cursor: hand; -fx-padding: 0;");
        } else {
            favoriteButton.setText("♡");  // Empty heart
            favoriteButton.setStyle("-fx-font-size: 28px; -fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-cursor: hand; -fx-padding: 0;");
        }
    }
    // LOAD AD DETAILS

    private void loadAdDetails(Long adId) {
        try {
            Map<String, Object> result = AdService.getAdById(adId);
            Advertisement ad = (Advertisement) result.get("ad");

            if (ad.getOwner() != null) {
                DataHolder.setSelectedUserId(ad.getOwner().getId());
            }

            // Populate UI fields
            titleLabel.setText(ad.getTitle());
            priceLabel.setText(ad.getPrice() + " T");
            categoryLabel.setText(ad.getCategory() != null ? ad.getCategory().getName() : "N/A");
            cityLabel.setText(ad.getCity() != null ? ad.getCity().getName() : "N/A");
            ownerLabel.setText(ad.getOwner() != null ? ad.getOwner().getFullName() : "Unknown");
            statusLabel.setText(ad.getStatus());
            dateLabel.setText(ad.getCreatedAt() != null ? ad.getCreatedAt().toString() : "N/A");
            descriptionArea.setText(ad.getDescription());
            displaySpecifications(ad);

            // Load favorite status and count
            loadFavoriteState(adId);


        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Ad Details Error",
                    "Unable to load ad details",
                    "There was a problem loading ad details. Please try again later.",
                    "ERROR"
            );
        }
    }

    private void displaySpecifications(Advertisement ad) {
        specificationsContainer.getChildren().clear();
        if (ad.getSpecificationDetails() != null && !ad.getSpecificationDetails().isEmpty()) {
            for (AdvertisementSpecification spec : ad.getSpecificationDetails()) {
                Label label = new Label(spec.getSpecification().getName() + ": " + spec.getValue());
                label.setStyle("-fx-font-size: 12;");
                specificationsContainer.getChildren().add(label);
            }
        } else {
            Label noSpecLabel = new Label("No specifications provided.");
            noSpecLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12;");
            specificationsContainer.getChildren().add(noSpecLabel);
        }
    }

    // GO TO RATING

    @FXML
    private void goToRating() throws Exception {
        if (adId != null) {
            try {
                Map<String,Object> result = AdService.getAdById(adId);
                Advertisement currentAd= (Advertisement) result.get("ad");

                Long sellerId = currentAd.getOwner().getId();
                Long adId = currentAd.getId();
                DataHolder.setSelectedAdId(adId);
                DataHolder.setSelectedUserId(sellerId);
                NavigationUtil.goToRating();

            }catch (Exception e){
                ShowErrorDialog.showErrorDialog(
                        "Rating Error",
                        "Failed to rate seller",
                        "There was a problem rating the seller. Please try again later.",
                        "ERROR"
                );
            }

        } else {
            ShowErrorDialog.showErrorDialog(
                    "Selection Error",
                    "No ad selected",
                    "Please select an ad",
                    "ERROR"
            );
        }
    }

    @FXML
    private void goToChat() throws Exception {
        Map<String, Object> adObj = AdService.getAdById(adId);
        Advertisement currentAd = (Advertisement) adObj.get("ad");
        if (currentAd == null || currentAd.getOwner() == null) {
            ShowErrorDialog.showErrorDialog(
                    "Chat Error",
                    "Cannot start chat: Ad or owner not found",
                    "There was a problem starting the chat. Please try again later.",
                    "ERROR"
            );
            return;
        }

        try {
            // Start conversation with the seller
            Long sellerId = currentAd.getOwner().getId();
            Long adId = currentAd.getId();

            Map<String, Object> result = ConversationService.startConversation(sellerId, adId);
            Long conversationId = (Long) result.get("id");

            // Store conversation ID and navigate to chat
            DataHolder.setSelectedConversationId(conversationId);
            NavigationUtil.goToChat();

        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Conversation Error",
                    "Failed to start conversation",
                    "There was a problem starting the conversation. Please try again later.",
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
package com.controller;

import com.model.Advertisement;
import com.model.User;
import com.service.AdService;
import com.service.ConversationService;
import com.util.DataHolder;
import com.util.NavigationUtil;
import com.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import com.service.FavoriteService;
import javafx.scene.control.Button;

import java.util.Map;

import java.util.Map;

public class AdDetailsController {

    // FXML FIELDS

    @FXML
    private Label titleLabel;

    @FXML
    private Label priceLabel;

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
    private Label errorLabel;

    @FXML
    private ImageView imageView;

    @FXML
    private VBox imageContainer;

    private Long adId;

    // Favorite UI elements
    @FXML private Button favoriteButton;
    @FXML private Label favoriteCountLabel;
    private Advertisement currentAd;
    private boolean isFavorited = false;
    private Long favoriteCount = 0L;

    // INITIALIZE

    @FXML
    private void initialize() {
        // Get ad ID from DataHolder
        adId = DataHolder.getSelectedAdId();  

        if (adId == null) {
            errorLabel.setText("No ad selected");
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
            errorLabel.setText("Failed to load favorite status: " + e.getMessage());
        }
    }

    // LOAD AD DETAILS

    private void loadAdDetails(Long adId) {
        try {
            Map<String, Object> result = AdService.getAdById(adId);
            Advertisement ad = (Advertisement) result.get("ad");

            // Populate UI fields
            titleLabel.setText(ad.getTitle());
            priceLabel.setText(ad.getPrice() + " T");
            categoryLabel.setText(ad.getCategory() != null ? ad.getCategory().getName() : "N/A");
            cityLabel.setText(ad.getCity() != null ? ad.getCity().getName() : "N/A");
            ownerLabel.setText(ad.getOwner() != null ? ad.getOwner().getFullName() : "Unknown");
            statusLabel.setText(ad.getStatus());
            dateLabel.setText(ad.getCreatedAt() != null ? ad.getCreatedAt().toString() : "N/A");
            descriptionArea.setText(ad.getDescription());

            // Load favorite status and count
            loadFavoriteState(adId);

            errorLabel.setText("");

        } catch (Exception e) {
            errorLabel.setText("Failed to load ad details: " + e.getMessage());
        }
    }

    // GO TO RATING

    @FXML
    private void goToRating() {
        if (adId != null) {
            DataHolder.setSelectedAdId(adId);
            NavigationUtil.goToRating();
        } else {
            errorLabel.setText("No ad selected");
        }
    }

    @FXML
    private void goToChat() throws Exception {
        Map<String, Object> adObj = AdService.getAdById(adId);
        Advertisement currentAd = (Advertisement) adObj.get("ad");
        if (currentAd == null || currentAd.getOwner() == null) {
            errorLabel.setText("Cannot start chat: Ad or owner not found");
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
            errorLabel.setText("Failed to start conversation: " + e.getMessage());
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
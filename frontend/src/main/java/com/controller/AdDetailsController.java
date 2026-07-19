package com.controller;

import com.model.Advertisement;
import com.service.AdService;
import com.util.DataHolder;
import com.util.NavigationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

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

            //TODO: use isFavorited and favoriteCount

            // TODO: load all images
            // TODO: add addToFavorite button and then implement the logic
            //  from backend-service

            errorLabel.setText("");

        } catch (Exception e) {
            errorLabel.setText("Failed to load ad details: " + e.getMessage());
        }
    }

    // ============================================
    // GO TO RATING
    // ============================================

    @FXML
    private void goToRating() {
        if (adId != null) {
            DataHolder.setSelectedAdId(adId);
            NavigationUtil.goToRating();
        } else {
            errorLabel.setText("No ad selected");
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

    @FXML
    private void goToChat() {
        // TODO: Start conversation with seller
        // NavigationUtil.goToChat();
    }
}
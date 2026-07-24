package com.controller;

import com.model.Advertisement;
import com.model.AdvertisementSpecification;
import com.model.Image;
import com.model.Rating;
import com.model.User;
import com.service.AdService;
import com.service.ConversationService;
import com.service.FavoriteService;
import com.service.ImageService;
import com.service.RatingService;
import com.util.DataHolder;
import com.util.NavigationUtil;
import com.util.SessionManager;
import com.view.ShowErrorDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

public class AdDetailsController {

    // ============================================
    // FXML FIELDS
    // ============================================

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
    private VBox specificationsContainer;

    @FXML
    private Label errorLabel;

    // ===== Image Fields =====
    @FXML
    private ImageView mainImageView;

    @FXML
    private ListView<Image> thumbnailListView;

    @FXML
    private Label imageErrorLabel;

    // ===== Ratings Fields =====
    @FXML
    private ListView<Rating> ratingsListView;

    @FXML
    private Label ratingsErrorLabel;

    // ===== Favorite Fields =====
    @FXML
    private Button favoriteButton;

    @FXML
    private Label favoriteCountLabel;

    // ============================================
    // DATA
    // ============================================

    private Long adId;
    private User adOwner;
    private boolean isFavorited = false;
    private Long favoriteCount = 0L;

    private ObservableList<Rating> ratings = FXCollections.observableArrayList();
    private ObservableList<Image> thumbnailImages = FXCollections.observableArrayList();

    // ============================================
    // INITIALIZE
    // ============================================

    @FXML
    private void initialize() {
        adId = DataHolder.getSelectedAdId();

        if (adId == null) {
            ShowErrorDialog.showErrorDialog("Failed", "No ad selected", "Please select an ad", "ERROR");
            return;
        }

        // Setup ratings list view
        ratingsListView.setCellFactory(lv -> new ListCell<Rating>() {
            @Override
            protected void updateItem(Rating rating, boolean empty) {
                super.updateItem(rating, empty);
                if (empty || rating == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                String buyerName = rating.getBuyer() != null ?
                        rating.getBuyer().getUsername() : "Unknown";

                String stars = "⭐".repeat(Math.max(0, rating.getScore()));

                String displayText = String.format(
                        "%s %d/5 by %s",
                        stars,
                        rating.getScore(),
                        buyerName
                );

                setText(displayText);
                setStyle("-fx-padding: 6 12; -fx-border-color: #333333; -fx-border-width: 0 0 1 0;");
            }
        });

        // Setup thumbnail list view
        thumbnailListView.setItems(thumbnailImages);
        thumbnailListView.setCellFactory(lv -> new ListCell<Image>() {
            @Override
            protected void updateItem(Image img, boolean empty) {
                super.updateItem(img, empty);
                if (empty || img == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                try {
                    String url = ImageService.getImageUrl(img.getId());
                    javafx.scene.image.Image fxImage = new javafx.scene.image.Image(url, true);
                    ImageView thumbView = new ImageView(fxImage);
                    thumbView.setFitHeight(60);
                    thumbView.setFitWidth(60);
                    thumbView.setPreserveRatio(true);
                    setGraphic(thumbView);
                } catch (Exception e) {
                    setText("Error");
                }
            }
        });

        // Thumbnail click handler
        thumbnailListView.setOnMouseClicked(event -> {
            Image selected = thumbnailListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    String url = ImageService.getImageUrl(selected.getId());
                    mainImageView.setImage(new javafx.scene.image.Image(url, true));
                } catch (Exception e) {
                    imageErrorLabel.setText("Failed to load image: " + e.getMessage());
                }
            }
        });

        // Double-click on owner label to see seller's ads
        ownerLabel.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                openSellerAds();
            }
        });
        ownerLabel.setStyle("-fx-cursor: hand; -fx-underline: true;");

        // Load ad details
        loadAdDetails(adId);

        DataHolder.clearSelectedAdId();
    }

    // ============================================
    // LOAD AD DETAILS
    // ============================================

    private void loadAdDetails(Long adId) {
        try {
            Map<String, Object> result = AdService.getAdById(adId);
            Advertisement ad = (Advertisement) result.get("ad");

            if (ad.getOwner() != null) {
                DataHolder.setSelectedUserId(ad.getOwner().getId());
                adOwner = ad.getOwner();
            }

            // Populate UI fields
            titleLabel.setText(ad.getTitle());
            priceLabel.setText(ad.getPrice() + " T");
            categoryLabel.setText(ad.getCategory() != null ? ad.getCategory().getName() : "N/A");
            cityLabel.setText(ad.getCity() != null ? ad.getCity().getName() : "N/A");
            ownerLabel.setText(ad.getOwner() != null ? ad.getOwner().getUsername() : "Unknown");
            statusLabel.setText(ad.getStatus());
            dateLabel.setText(ad.getCreatedAt() != null ? ad.getCreatedAt().toString() : "N/A");
            descriptionArea.setText(ad.getDescription());

            // Load ad sections
            displaySpecifications(ad);
            loadImages(adId);
            loadRatings(adId);
            loadFavoriteState(adId);

            // Ratings placeholder
            if (ratings.isEmpty()) {
                ratingsListView.setPlaceholder(new Label("No ratings yet for this ad."));
            } else {
                ratingsListView.setPlaceholder(null);
            }

        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Ad Details Error",
                    "Unable to load ad details",
                    e.getMessage(),
                    "ERROR"
            );
        }
    }

    // ============================================
    // LOAD IMAGES
    // ============================================

    private void loadImages(Long adId) {
        try {
            List<Image> imageList = ImageService.getImagesByAd(adId);
            thumbnailImages.clear();
            thumbnailImages.addAll(imageList);
            thumbnailListView.setItems(thumbnailImages);

            if (imageList.isEmpty()) {
                thumbnailListView.setPlaceholder(new Label("No images for this ad"));
                mainImageView.setImage(new javafx.scene.image.Image(
                        getClass().getResourceAsStream("/view/images/default-ad-placeholder.png")));
                imageErrorLabel.setText("");
                return;
            }

            // Find primary image or use first
            Image primaryImage = imageList.stream()
                    .filter(Image::isPrimary)
                    .findFirst()
                    .orElse(imageList.get(0));

            String imageUrl = ImageService.getImageUrl(primaryImage.getId());
            mainImageView.setImage(new javafx.scene.image.Image(imageUrl, true));

            imageErrorLabel.setText("");

        } catch (Exception e) {
            imageErrorLabel.setText("Failed to load images: " + e.getMessage());
            mainImageView.setImage(new javafx.scene.image.Image(
                    getClass().getResourceAsStream("/images/default-ad-placeholder.png")));
        }
    }

    // ============================================
    // LOAD RATINGS
    // ============================================

    private void loadRatings(Long adId) {
        try {
            List<Rating> ratingList = RatingService.getRatingsByAdvertisement(adId);
            ratings.clear();
            ratings.addAll(ratingList);
            ratingsListView.setItems(ratings);
            ratingsErrorLabel.setText("");
        } catch (Exception e) {
            ratingsErrorLabel.setText("Failed to load ratings: " + e.getMessage());
        }
    }

    // ============================================
    // LOAD FAVORITE STATE
    // ============================================

    private void loadFavoriteState(Long adId) {
        try {
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser != null) {
                isFavorited = FavoriteService.isFavorited(adId);
                updateFavoriteButton();
            }

            favoriteCount = FavoriteService.getFavoriteCount(adId);
            favoriteCountLabel.setText(String.valueOf(favoriteCount));

        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Favorite Status Error",
                    "Unable to load favorite information",
                    e.getMessage(),
                    "ERROR"
            );
        }
    }

    // ============================================
    // FAVORITE BUTTON
    // ============================================

    @FXML
    private void toggleFavorite() {
        try {
            if (isFavorited) {
                FavoriteService.removeFavorite(adId);
                isFavorited = false;
                favoriteCount--;
            } else {
                FavoriteService.addToFavorite(adId);
                isFavorited = true;
                favoriteCount++;
            }

            updateFavoriteButton();
            favoriteCountLabel.setText(String.valueOf(favoriteCount));

        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Update Favorite Error",
                    "Failed to update favorite",
                    e.getMessage(),
                    "ERROR"
            );
        }
    }

    private void updateFavoriteButton() {
        if (isFavorited) {
            favoriteButton.setText("♥");
            favoriteButton.setStyle("-fx-font-size: 28px; -fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-cursor: hand; -fx-padding: 0;");
        } else {
            favoriteButton.setText("♡");
            favoriteButton.setStyle("-fx-font-size: 28px; -fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-cursor: hand; -fx-padding: 0;");
        }
    }

    // ============================================
    // DISPLAY SPECIFICATIONS
    // ============================================

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

    // ============================================
    // OPEN SELLER ADS
    // ============================================

    private void openSellerAds() {
        if (adOwner == null) {
            ShowErrorDialog.showErrorDialog("Error", "No seller information", "Seller not found.", "ERROR");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/my-ads.fxml"));
            Parent root = loader.load();
            UserAdController controller = loader.getController();

            DataHolder.setSelectedUser(adOwner);
            controller.setTargetUser(adOwner);
            controller.setHeaderText("Ads of " + adOwner.getUsername());
            controller.loadAdsForUser();

            Stage stage = new Stage();
            stage.setTitle("Ads of " + adOwner.getUsername());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            ShowErrorDialog.showErrorDialog(
                    "Error",
                    "Failed to open seller ads",
                    e.getMessage(),
                    "ERROR"
            );
        }
    }

    // ============================================
    // GO TO RATING
    // ============================================

    @FXML
    private void goToRating() {
        if (adId == null) {
            ShowErrorDialog.showErrorDialog(
                    "Selection Error",
                    "No ad selected",
                    "Please select an ad",
                    "ERROR"
            );
            return;
        }

        try {
            Map<String, Object> result = AdService.getAdById(adId);
            Advertisement currentAd = (Advertisement) result.get("ad");

            if (currentAd.getOwner() == null) {
                ShowErrorDialog.showErrorDialog(
                        "Rating Error",
                        "Seller not found",
                        "Unable to find seller for this ad.",
                        "ERROR"
                );
                return;
            }

            Long sellerId = currentAd.getOwner().getId();
            DataHolder.setSelectedAdId(adId);
            DataHolder.setSelectedUserId(sellerId);
            NavigationUtil.goToRating();

        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Rating Error",
                    "Failed to rate seller",
                    e.getMessage(),
                    "ERROR"
            );
        }
    }

    // ============================================
    // GO TO CHAT
    // ============================================

    @FXML
    private void goToChat() {
        if (adId == null) {
            ShowErrorDialog.showErrorDialog(
                    "Chat Error",
                    "No ad selected",
                    "Please select an ad",
                    "ERROR"
            );
            return;
        }

        try {
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

            Long sellerId = currentAd.getOwner().getId();
            Map<String, Object> result = ConversationService.startConversation(sellerId, adId);
            Long conversationId = ((Number) result.get("id")).longValue();

            DataHolder.setSelectedConversationId(conversationId);
            NavigationUtil.goToChat();

        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Conversation Error",
                    "Failed to start conversation",
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
}
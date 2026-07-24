package com.view;

import com.model.Advertisement;
import com.model.Image;
import com.service.ImageService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class AdCell extends ListCell<Advertisement> {

    // ============================================
    // UI COMPONENTS
    // ============================================

    private final VBox cardLayout = new VBox(6);
    private final ImageView imageView = new ImageView();
    private final Label titleLabel = new Label();
    private final Label priceLabel = new Label();
    private final Label sellerLabel = new Label();   // <-- NEW
    private final Label cityLabel = new Label();
    private final Label dateLabel = new Label();
    private final Label statusLabel = new Label();

    // ============================================
    // CONSTRUCTOR
    // ============================================

    public AdCell() {
        // Configure ImageView
        imageView.setFitWidth(180);
        imageView.setFitHeight(135);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-background-color: #2A2A2A; -fx-border-color: #333333; -fx-border-radius: 6; -fx-background-radius: 6;");

        // Configure labels
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #FFFFFF;");
        titleLabel.setWrapText(true);

        priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #4CAF50;");

        sellerLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #BDBDBD;");   // <-- NEW

        cityLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9E9E9E;");
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9E9E9E;");

        statusLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
        statusLabel.setVisible(false);

        // Build layout
        HBox infoBox = new HBox(12);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.getChildren().add(imageView);

        VBox textBox = new VBox(3);
        textBox.getChildren().addAll(titleLabel, priceLabel, sellerLabel, cityLabel, dateLabel, statusLabel); // <-- added sellerLabel
        VBox.setVgrow(textBox, Priority.ALWAYS);

        infoBox.getChildren().add(textBox);
        infoBox.setPadding(new Insets(8, 12, 8, 12));

        cardLayout.getChildren().add(infoBox);
        cardLayout.setStyle("-fx-background-color: #1A1A1A; -fx-border-color: #2A2A2A; -fx-border-radius: 8; -fx-background-radius: 8;");
        cardLayout.setPadding(new Insets(4, 4, 4, 4));
    }

    // ============================================
    // UPDATE ITEM
    // ============================================

    @Override
    protected void updateItem(Advertisement ad, boolean empty) {
        super.updateItem(ad, empty);

        if (empty || ad == null) {
            setGraphic(null);
            setText(null);
            return;
        }

        // Set text data
        titleLabel.setText(ad.getTitle());
        priceLabel.setText(formatPrice(ad.getPrice()));

        // Seller/owner name  <-- NEW
        sellerLabel.setText(ad.getOwner() != null ? "by " + ad.getOwner().getUsername() : "Unknown seller");

        cityLabel.setText(ad.getCity() != null ? ad.getCity().getName() : "Unknown");

        // Set relative date
        if (ad.getCreatedAt() != null) {
            dateLabel.setText(ad.getCreatedAt().toLocalDate().toString());
        } else {
            dateLabel.setText("");
        }

        // Status label
        String status = ad.getStatus();
        if (status != null && !"ACCEPTED".equals(status)) {
            statusLabel.setText(status);
            statusLabel.setVisible(true);
            switch (status) {
                case "SOLD":
                    statusLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold; -fx-font-size: 11px;");
                    break;
                case "PENDING":
                    statusLabel.setStyle("-fx-text-fill: #F39C12; -fx-font-weight: bold; -fx-font-size: 11px;");
                    break;
                case "REJECTED":
                    statusLabel.setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold; -fx-font-size: 11px;");
                    break;
                default:
                    statusLabel.setStyle("-fx-text-fill: #9E9E9E; -fx-font-weight: bold; -fx-font-size: 11px;");
                    break;
            }
        } else {
            statusLabel.setVisible(false);
        }

        // Load and set image
        loadImage(ad);

        setGraphic(cardLayout);
        setPadding(new Insets(4, 8, 4, 8));
    }

    // ============================================
    // HELPERS
    // ============================================

    private void loadImage(Advertisement ad) {
        try {
            List<Image> images = ImageService.getImagesByAd(ad.getId());
            if (!images.isEmpty()) {
                Image firstImage = images.get(0);
                String url = ImageService.getImageUrl(firstImage.getId());
                javafx.scene.image.Image fxImage = new javafx.scene.image.Image(url, true);
                imageView.setImage(fxImage);
            } else {
                setDefaultImage();
            }
        } catch (Exception e) {
            setDefaultImage();
        }
    }

    private void setDefaultImage() {
        javafx.scene.image.Image defaultImage = new javafx.scene.image.Image(
                getClass().getResourceAsStream("/view/images/default-ad-placeholder.png"));
        imageView.setImage(defaultImage);
        imageView.setStyle("-fx-background-color: #2A2A2A; -fx-border-color: #333333; -fx-border-radius: 6; -fx-background-radius: 6;");
    }

    private String formatPrice(Long price) {
        if (price == null) return "0 T";
        return String.format("%,d T", price);
    }
}
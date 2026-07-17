package com.controller;

import com.model.Advertisement;
import com.model.Favorite;
import com.service.FavoriteService;
import com.util.NavigationUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class FavoriteController {

    // FXML FIELDS

    @FXML
    private ListView<Advertisement> favoritesListView;

    @FXML
    private Label errorLabel;

    @FXML
    private Label countLabel;

    @FXML
    private Button refreshButton;

    // DATA

    private ObservableList<Advertisement> favorites = FXCollections.observableArrayList();

    // INITIALIZE

    @FXML
    private void initialize() {
        // Setup custom cell rendering for favorites
        favoritesListView.setCellFactory(lv -> new FavoriteCell());

        // Handle double-click on ad to show details
        favoritesListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Advertisement selected = favoritesListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    NavigationUtil.goToAdDetails(selected.getId());
                }
            }
        });

        // Load favorites from backend
        loadFavorites();
    }

    // Refresh the page
    @FXML
    private void refreshButton() {
        loadFavorites();  // Simply reloads data
    }

    // LOAD FAVORITES

    @FXML
    private void loadFavorites() {
        try {
            // Fetch favorites from backend
            List<Favorite> favoriteList = FavoriteService.getFavorites();

            // Clear existing list
            favorites.clear();

            // Extract advertisements from favorites
            for (Favorite fav : favoriteList) {
                Advertisement ad = fav.getAdvertisement();
                if (ad != null) {
                    favorites.add(ad);
                }
            }

            // Update ListView
            favoritesListView.setItems(favorites);

            // Update count label
            countLabel.setText("You have " + favorites.size() + " favorite ad(s)");

            errorLabel.setText("");

        } catch (Exception e) {
            errorLabel.setText("Failed to load favorites: " + e.getMessage());
        }
    }

    // REMOVE FROM FAVORITES

    @FXML
    private void removeSelectedFavorite() {
        Advertisement selected = favoritesListView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            errorLabel.setText("Please select an ad to remove");
            return;
        }

        try {
            // Call backend to remove favorite
            FavoriteService.removeFavorite(selected.getId());

            // Remove from local list
            favorites.remove(selected);

            // Update count label
            countLabel.setText("You have " + favorites.size() + " favorite ad(s)");

            errorLabel.setText("Ad removed from favorites successfully");

        } catch (Exception e) {
            errorLabel.setText("Failed to remove favorite: " + e.getMessage());
        }
    }

    // NAVIGATION

    @FXML
    private void goToHome() {
        NavigationUtil.goToHome();
    }

    @FXML
    private void goToAdDetails() {
        Advertisement selected = favoritesListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            NavigationUtil.goToAdDetails(selected.getId());
        } else {
            errorLabel.setText("Please select an ad to view");
        }
    }

    // INNER CLASS: FavoriteCell (Custom List View Cell)

    private class FavoriteCell extends ListCell<Advertisement> {

        private final VBox cellLayout = new VBox(5);
        private final Label titleLabel = new Label();
        private final Label priceLabel = new Label();
        private final Label statusLabel = new Label();
        private final Button removeButton = new Button("✕");
        private final HBox contentBox = new HBox(10);

        public FavoriteCell() {
            // Style the remove button
            removeButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-font-weight: bold;");
            removeButton.setPrefSize(30, 30);

            // Style the labels
            titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            priceLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
            statusLabel.setStyle("-fx-font-size: 12px;");

            // Handle remove button click
            removeButton.setOnAction(event -> {
                Advertisement ad = getItem();  // ✅ Get the current item
                if (ad != null) {
                    try {
                        FavoriteService.removeFavorite(ad.getId());

                        // Update the UI from the cell
                        Platform.runLater(() -> {
                            favorites.remove(ad);
                            countLabel.setText("You have " + favorites.size() + " favorite ad(s)");
                            errorLabel.setText("Ad removed from favorites successfully");
                        });

                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            errorLabel.setText("Failed to remove favorite: " + e.getMessage());
                        });
                    }
                }
            });

            // Layout
            VBox infoBox = new VBox(3, titleLabel, priceLabel, statusLabel);
            contentBox.getChildren().addAll(infoBox, removeButton);
            contentBox.setSpacing(10);

            cellLayout.getChildren().add(contentBox);
            cellLayout.setStyle("-fx-padding: 10; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        }


        @Override
        protected void updateItem(Advertisement ad, boolean empty) {
            super.updateItem(ad, empty);

            if (empty || ad == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            // Set data
            titleLabel.setText(ad.getTitle());
            priceLabel.setText("Price: " + ad.getPrice() + " T");
            statusLabel.setText("Status: " + (ad.getStatus() != null ? ad.getStatus() : "N/A"));

            // Color status based on value
            if ("ACTIVE".equals(ad.getStatus())) {
                statusLabel.setStyle("-fx-text-fill: #2e7d32;");
            } else if ("SOLD".equals(ad.getStatus())) {
                statusLabel.setStyle("-fx-text-fill: #ff9800;");
            } else if ("REJECTED".equals(ad.getStatus())) {
                statusLabel.setStyle("-fx-text-fill: #f44336;");
            } else {
                statusLabel.setStyle("-fx-text-fill: #9e9e9e;");
            }

            setGraphic(cellLayout);
        }
    }
}
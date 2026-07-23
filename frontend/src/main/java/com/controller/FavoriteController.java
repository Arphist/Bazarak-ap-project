package com.controller;

import com.model.Advertisement;
import com.model.Favorite;
import com.service.FavoriteService;
import com.util.NavigationUtil;
import com.view.AdCell;
import com.view.ShowErrorDialog;
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
    private Label countLabel;

    @FXML
    private Button refreshButton;

    // DATA

    private ObservableList<Advertisement> favorites = FXCollections.observableArrayList();

    // INITIALIZE

    @FXML
    private void initialize() {
        // Setup custom cell rendering for favorites
        favoritesListView.setCellFactory(lv -> new AdCell());

        // Handle double-click on ad to show details
        favoritesListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Advertisement selected = favoritesListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    NavigationUtil.goToAdDetails(selected.getId());
                }
            }
        });

        if (favorites.isEmpty()) {
            favoritesListView.setPlaceholder(new Label("No favorited ads found"));
        } else {
            favoritesListView.setPlaceholder(null);
        }

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



        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Favorites Error",
                    "Failed to load favorites",
                    e.getMessage(),
                    "ERROR"
            );
        }
    }

    // REMOVE FROM FAVORITES

    @FXML
    private void removeSelectedFavorite() {
        Advertisement selected = favoritesListView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            ShowErrorDialog.showErrorDialog(
                    "Selection Error",
                    "No advertisement selected",
                    "Please select an ad to remove.",
                    "WARNING"
            );
            return;
        }

        try {
            FavoriteService.removeFavorite(selected.getId());
            favorites.remove(selected);
            countLabel.setText("You have " + favorites.size() + " favorite ad(s)");

            ShowErrorDialog.showErrorDialog(
                    "Success",
                    null,
                    "Advertisement removed from favorites successfully.",
                    "INFORMATION"
            );

        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Favorite Error",
                    "Failed to remove favorite",
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
    private void goToAdDetails() {
        Advertisement selected = favoritesListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            NavigationUtil.goToAdDetails(selected.getId());
        } else {
            ShowErrorDialog.showErrorDialog(
                    "Selection Error",
                    "No advertisement selected",
                    "Please select an ad to view.",
                    "WARNING"
            );
        }
    }


}
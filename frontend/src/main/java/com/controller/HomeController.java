package com.controller;

import com.BazarakFrontendApplication;
import com.model.Advertisement;
import com.model.User;
import com.service.AdService;
import com.util.DataHolder;
import com.util.NavigationUtil;
import com.util.SessionManager;
import com.view.AdCell;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class HomeController {

    // FXML FIELDS

    @FXML
    private TextField searchField;

    @FXML
    private ListView<Advertisement> adListView;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label errorLabel;

    @FXML
    private Button adminButton;

    // INITIALIZE

    @FXML
    private void initialize() {
        // Setup custom cell rendering for ads
        adListView.setCellFactory(lv -> new ListCell<Advertisement>() {
            @Override
            protected void updateItem(Advertisement ad, boolean empty) {
                super.updateItem(ad, empty);
                if (empty || ad == null) {
                    setText("No ads found");
                } else {
                    // Display ad title and price
                    setText(ad.getTitle() + " - " + ad.getPrice() + " T");
                }
            }
        });
        // Handle double-click on ad to show details
        adListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Advertisement selected = adListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    DataHolder.setSelectedAdId(selected.getId());
                    BazarakFrontendApplication.showAdDetailsPage();
                }
            }
        });

        // Search on Enter key
        searchField.setOnAction(e -> handleSearch());

        // Get current user from session
        User currentUser = SessionManager.getCurrentUser();

        // Show welcome message if user is logged in
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getFullName() + "!");
        }

        // Show admin button only for admin users
        if (adminButton != null) {
            adminButton.setVisible(SessionManager.isAdmin());
        }


        // Load ads from backend
        loadAds();
    }

    // LOAD ADS

    private void loadAds() {
        try {
            List<Advertisement> ads = AdService.getActiveAds();
            adListView.setItems(FXCollections.observableArrayList(ads));
            adListView.setCellFactory(lv -> new AdCell());
        } catch (Exception e) {
            errorLabel.setText("Failed to load ads: " + e.getMessage());
        }
    }

    // SEARCH

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();

        try {
            List<Advertisement> results;

            // If no keyword, show all active ads
            if (keyword.isEmpty()) {
                results = AdService.getActiveAds();
            } else {
                results = AdService.searchAds(keyword);
            }

            if (results.isEmpty()) {
                adListView.setPlaceholder(new Label("No ads found"));
            }

            // Update list view with search results
            adListView.getItems().clear();
            adListView.getItems().addAll(results);


            errorLabel.setText("");

        } catch (Exception e) {
            errorLabel.setText("Search failed: " + e.getMessage());
        }
    }

    // NAVIGATION

    @FXML
    private void goToCreateAd() {
        NavigationUtil.goToCreateAd();
    }

    @FXML
    private void goToFavorites() {
        NavigationUtil.goToFavorites();
    }

    @FXML
    private void goToProfile() {
        NavigationUtil.goToProfile();
    }

    @FXML
    private void goToAdmin() {
        NavigationUtil.goToAdminDashboard();
    }

    @FXML
    private void goToMyAds() {
        NavigationUtil.goToMyAds();
    }

    @FXML
    private void goToChat() {
        NavigationUtil.goToChat();
    }

    // LOGOUT
    @FXML
    private void handleLogout() {
        NavigationUtil.logout();  // Handles logout + navigation
    }
}
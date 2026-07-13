package com.controller;

import com.BazarakFrontendApplication;
import com.model.Advertisement;
import com.model.User;
import com.service.AdService;
import com.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.util.List;

public class MainController {

    // ============================================
    // FXML FIELDS
    // ============================================

    @FXML
    private TextField searchField;

    @FXML
    private ListView<String> adListView;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label errorLabel;

    @FXML
    private Button adminButton;

    // ============================================
    // INITIALIZE
    // ============================================

    @FXML
    private void initialize() {
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

    // ============================================
    // LOAD ADS
    // ============================================

    private void loadAds() {
        try {
            // Fetch active ads from server
            List<Advertisement> ads = AdService.getActiveAds();

            // Clear existing list
            adListView.getItems().clear();

            // Add each ad to the list view
            for (Advertisement ad : ads) {
                String displayText = ad.getTitle() + " - " + ad.getPrice() + " T";
                adListView.getItems().add(displayText);
            }

            errorLabel.setText("");

        } catch (Exception e) {
            errorLabel.setText("Failed to load ads: " + e.getMessage());
        }
    }
    // ============================================
    // SEARCH
    // ============================================

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

            // Update list view with search results
            adListView.getItems().clear();
            for (Advertisement ad : results) {
                adListView.getItems().add(ad.getTitle() + " - " + ad.getPrice() + " T");
            }

            errorLabel.setText("");

        } catch (Exception e) {
            errorLabel.setText("Search failed: " + e.getMessage());
        }
    }
    // ============================================
    // NAVIGATION
    // ============================================

    @FXML
    private void goToCreateAd() {
        BazarakFrontendApplication.showPostAdPage();
    }

    @FXML
    private void goToFavorites() {
        BazarakFrontendApplication.showFavoritesPage();
    }

    @FXML
    private void goToProfile() {
        BazarakFrontendApplication.showProfilePage();
    }

    @FXML
    private void goToAdmin() {
        BazarakFrontendApplication.showAdminDashboard();
    }

    // ============================================
    // LOGOUT
    // ============================================

    @FXML
    private void handleLogout() {
        try {
            com.service.AuthService.logout();
            BazarakFrontendApplication.showLoginPage();
        } catch (Exception e) {
            errorLabel.setText("Logout failed: " + e.getMessage());
        }
    }



}
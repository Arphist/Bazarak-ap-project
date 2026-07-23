package com.controller;

import com.BazarakFrontendApplication;
import com.model.*;
import com.service.*;
import com.util.DataHolder;
import com.util.NavigationUtil;
import com.util.SessionManager;
import com.view.AdCell;
import com.view.ShowErrorDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;

public class HomeController {

    // FXML FIELDS

    @FXML
    private TextField searchField;

    @FXML
    private ListView<Advertisement> adListView;

    @FXML
    private Label welcomeLabel;

    // Advanced search fields
    @FXML
    private ComboBox<Category> categoryCombo;

    @FXML
    private ComboBox<City> cityCombo;

    @FXML
    private TextField minPriceField;

    @FXML
    private TextField maxPriceField;

    @FXML
    private ComboBox<String> sortByCombo;

    @FXML
    private Button clearFiltersButton;

    @FXML
    private VBox filterContainer;

    @FXML
    private ToggleButton filterToggleButton;

    // INITIALIZE

    @FXML
    private void initialize() {
        // =====  AdCell  =====
        adListView.setCellFactory(lv -> new AdCell());

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

        // Load data from backend
        loadCategories();
        loadCities();
        loadSortOptions();
        loadAds();

        // Hide filters by default
        if (filterContainer != null) {
            filterContainer.setVisible(false);
            filterContainer.setManaged(false);
        }
    }

    // LOAD DATA

    private void loadCategories() {
        try {
            List<Category> categories = CategoryService.getAllCategories();
            categoryCombo.setItems(FXCollections.observableArrayList(categories));
            categoryCombo.setPromptText("All Categories");
            // Add a "null" option (show all)
            Category allCategory = new Category();
            allCategory.setId(null);
            allCategory.setName("All Categories");
            // We'll handle this differently - use null selection
        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Category Error",
                    "Failed to load categories",
                    e.getMessage(),
                    "ERROR"
            );
        }
    }

    private void loadCities() {
        try {
            List<City> cities = CityService.getAllCities();
            cityCombo.setItems(FXCollections.observableArrayList(cities));
            cityCombo.setPromptText("All Cities");
        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "City Error",
                    "Failed to load cities",
                    e.getMessage(),
                    "ERROR"
            );
        }
    }

    private void loadSortOptions() {
        sortByCombo.setItems(FXCollections.observableArrayList(
                "Newest First",
                "Oldest First",
                "Price: Low to High",
                "Price: High to Low",
                "Title: A to Z",
                "Title: Z to A"
        ));
        sortByCombo.setValue("Newest First");
    }

    // LOAD ADS

    private void loadAds() {

        try {
            List<Advertisement> ads = AdService.getActiveAds();
            adListView.setItems(FXCollections.observableArrayList(ads));
            adListView.setCellFactory(lv -> new AdCell());
        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Advertisement Error",
                    "Failed to load ads",
                    e.getMessage(),
                    "ERROR"
            );
        }
    }

    // SEARCH

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();

        try {
            // Get filter values
            Long categoryId = categoryCombo.getValue() != null ? categoryCombo.getValue().getId() : null;
            Long cityId = cityCombo.getValue() != null ? cityCombo.getValue().getId() : null;
            Long minPrice = parsePrice(minPriceField.getText());
            Long maxPrice = parsePrice(maxPriceField.getText());

            // Parse sort options
            String sortBy = parseSortBy(sortByCombo.getValue());
            String sortOrder = parseSortOrder(sortByCombo.getValue());

            List<Advertisement> results;

            // If no keyword and no filters, show all active ads
            if (keyword.isEmpty() && categoryId == null && cityId == null &&
                    minPrice == null && maxPrice == null) {
                results = AdService.getActiveAds();
            } else {
                // Call the full search with all filters
                results = AdService.searchAds(
                        keyword.isEmpty() ? null : keyword,
                        categoryId,
                        cityId,
                        minPrice,
                        maxPrice,
                        sortBy,
                        sortOrder
                );
            }

            updateAdListView(results);

        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Search Error",
                    "Search failed",
                    e.getMessage(),
                    "ERROR"
            );
        }
    }

    // CLEAR FILTERS

    @FXML
    private void clearFilters() {
        searchField.clear();
        categoryCombo.setValue(null);
        cityCombo.setValue(null);
        minPriceField.clear();
        maxPriceField.clear();
        sortByCombo.setValue("Newest First");
        loadAds();
    }

    // TOGGLE FILTERS

    @FXML
    private void toggleFilters() {
        if (filterContainer != null) {
            boolean visible = !filterContainer.isVisible();
            filterContainer.setVisible(visible);
            filterContainer.setManaged(visible);
            if (filterToggleButton != null) {
                filterToggleButton.setText(visible ? "Hide Filters ▲" : "Show Filters ▼");
            }
        }
    }

    @FXML
    private void refreshAds() {
        // 1. Clear search field
        searchField.clear();

        // 2. Clear all filters
        clearFilters();

        // 3. Hide filters container if visible
        if (filterContainer != null && filterContainer.isVisible()) {
            filterContainer.setVisible(false);
            filterContainer.setManaged(false);
            if (filterToggleButton != null) {
                filterToggleButton.setText("Show Filters ▼");
            }
        }

        // 4. Reload ads
        loadAds();

    }

    // HELPER METHODS

    private Long parsePrice(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            String clean = text.trim().replace(",", "");
            return Long.parseLong(clean);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String parseSortBy(String value) {
        if (value == null) return "created_at";
        switch (value) {
            case "Oldest First":
            case "Newest First":
                return "created_at";
            case "Price: Low to High":
            case "Price: High to Low":
                return "price";
            case "Title: A to Z":
            case "Title: Z to A":
                return "title";
            default:
                return "created_at";
        }
    }

    private String parseSortOrder(String value) {
        if (value == null) return "desc";
        switch (value) {
            case "Oldest First":
                return "asc";
            case "Newest First":
                return "desc";
            case "Price: Low to High":
                return "asc";
            case "Price: High to Low":
                return "desc";
            case "Title: A to Z":
                return "asc";
            case "Title: Z to A":
                return "desc";
            default:
                return "desc";
        }
    }

    private void updateAdListView(List<Advertisement> ads) {
        adListView.getItems().clear();
        if (ads.isEmpty()) {
            adListView.setPlaceholder(new Label("No ads found"));
        } else {
            adListView.setPlaceholder(null);
            adListView.getItems().addAll(ads);
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
    private void goToMyAds() {
        NavigationUtil.goToMyAds();
    }

    @FXML
    private void goToMyRatings() {NavigationUtil.goToMyRatings();}

    @FXML
    private void goToChat() {
        NavigationUtil.goToChat();
    }

    // LOGOUT
    @FXML
    private void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Sign Out");
        confirm.setHeaderText("Are you sure you want to sign out?");
        confirm.setContentText("You will be redirected to the login page.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                com.service.AuthService.logout();
                NavigationUtil.goToLogin();
            } catch (Exception e) {
                ShowErrorDialog.showErrorDialog(
                        "Logout Error",
                        "Failed to sign out",
                        "There was a problem signing out. Please try again.",
                        "ERROR"
                );
            }
        }
    }
}
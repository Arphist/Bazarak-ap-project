package com.controller;

import com.BazarakFrontendApplication;
import com.model.*;
import com.service.*;
import com.util.DataHolder;
import com.util.NavigationUtil;
import com.util.SessionManager;
import com.view.AdCell;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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

    @FXML
    private Label errorLabel;

    @FXML
    private Button adminButton;

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
    private ComboBox<String> sortOrderCombo;

    @FXML
    private Button clearFiltersButton;

    @FXML
    private VBox filterContainer;

    @FXML
    private ToggleButton filterToggleButton;

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
            errorLabel.setText("Failed to load categories: " + e.getMessage());
        }
    }

    private void loadCities() {
        try {
            List<City> cities = CityService.getAllCities();
            cityCombo.setItems(FXCollections.observableArrayList(cities));
            cityCombo.setPromptText("All Cities");
        } catch (Exception e) {
            errorLabel.setText("Failed to load cities: " + e.getMessage());
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

        sortOrderCombo.setItems(FXCollections.observableArrayList(
                "Descending",
                "Ascending"
        ));
        sortOrderCombo.setValue("Descending");
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
            // Get filter values
            Long categoryId = categoryCombo.getValue() != null ? categoryCombo.getValue().getId() : null;
            Long cityId = cityCombo.getValue() != null ? cityCombo.getValue().getId() : null;
            Long minPrice = parsePrice(minPriceField.getText());
            Long maxPrice = parsePrice(maxPriceField.getText());

            // Parse sort options
            String sortBy = parseSortBy(sortByCombo.getValue());
            String sortOrder = parseSortOrder(sortOrderCombo.getValue());

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
            errorLabel.setText("");

        } catch (Exception e) {
            errorLabel.setText("Search failed: " + e.getMessage());
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
        sortOrderCombo.setValue("Descending");
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

    // HELPER METHODS

    //TODO: I might delete this
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
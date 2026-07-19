package com.controller;

import com.BazarakFrontendApplication;
import com.model.*;
import com.service.*;
import com.util.DataHolder;
import com.util.NavigationUtil;
import com.util.SessionManager;
import com.view.AdCell;
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

    @FXML
    private Label errorLabel;

    @FXML
    private Button adminButton;

    // ===== TEST DATA (TODO: remove after testing) =====
    private boolean useTestData = true;
    private ObservableList<Advertisement> testAds = FXCollections.observableArrayList();

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

        // ===== TEST DATA (TODO: remove after testing) =====
        if (useTestData) {
            createTestAds();
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
    }

    // LOAD ADS

    private void loadAds() {

        try {
            //TODO: remove this after testing
            if (useTestData) {
                return;
            }
            //-----//
            List<Advertisement> ads = AdService.getActiveAds();
            adListView.setItems(FXCollections.observableArrayList(ads));
            adListView.setCellFactory(lv -> new AdCell());
        } catch (Exception e) {
            errorLabel.setText("Failed to load ads: " + e.getMessage());
        }
    }

    // ===== TEST DATA (TODO: remove this whole method after testing) =====
    private void createTestAds() {
        testAds.clear();

        User testUser = SessionManager.getCurrentUser();
        if (testUser == null) {
            testUser = new User();
            testUser.setId(1L);
            testUser.setUsername("testuser");
            testUser.setFullName("Test User");
        }

        Category electronics = new Category();
        electronics.setId(1L);
        electronics.setName("Electronics");

        Category vehicles = new Category();
        vehicles.setId(2L);
        vehicles.setName("Vehicles");

        City tehran = new City();
        tehran.setId(1L);
        tehran.setName("Tehran");

        City mashhad = new City();
        mashhad.setId(2L);
        mashhad.setName("Mashhad");

        // Ad 1
        Advertisement ad1 = new Advertisement();
        ad1.setId(1L);
        ad1.setTitle("MacBook Pro 2023");
        ad1.setDescription("Used MacBook Pro 2023, 16GB RAM, 512GB SSD");
        ad1.setPrice(45000000L);
        ad1.setOwner(testUser);
        ad1.setCategory(electronics);
        ad1.setCity(tehran);
        ad1.setStatus("ACCEPTED");
        testAds.add(ad1);

        // Ad 2
        Advertisement ad2 = new Advertisement();
        ad2.setId(2L);
        ad2.setTitle("iPhone 14 Pro Max");
        ad2.setDescription("iPhone 14 Pro Max, 256GB, Deep Purple");
        ad2.setPrice(38000000L);
        ad2.setOwner(testUser);
        ad2.setCategory(electronics);
        ad2.setCity(mashhad);
        ad2.setStatus("ACCEPTED");
        testAds.add(ad2);

        // Ad 3
        Advertisement ad3 = new Advertisement();
        ad3.setId(3L);
        ad3.setTitle("Toyota Camry 2022");
        ad3.setDescription("40,000 km, full options, leather seats");
        ad3.setPrice(1200000000L);
        ad3.setOwner(testUser);
        ad3.setCategory(vehicles);
        ad3.setCity(tehran);
        ad3.setStatus("ACCEPTED");
        testAds.add(ad3);

        // Ad 4 (Pending)
        Advertisement ad4 = new Advertisement();
        ad4.setId(4L);
        ad4.setTitle("Samsung Galaxy S24 Ultra");
        ad4.setDescription("Brand new, 512GB, Titanium Black");
        ad4.setPrice(52000000L);
        ad4.setOwner(testUser);
        ad4.setCategory(electronics);
        ad4.setCity(tehran);
        ad4.setStatus("PENDING");
        testAds.add(ad4);

        // Ad 5 (Sold)
        Advertisement ad5 = new Advertisement();
        ad5.setId(5L);
        ad5.setTitle("Sony WH-1000XM5");
        ad5.setDescription("Noise-canceling headphones, like new");
        ad5.setPrice(8000000L);
        ad5.setOwner(testUser);
        ad5.setCategory(electronics);
        ad5.setCity(tehran);
        ad5.setStatus("SOLD");
        testAds.add(ad5);

        adListView.setItems(testAds);
        errorLabel.setText("🔧 " + testAds.size() + " test ads loaded");
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
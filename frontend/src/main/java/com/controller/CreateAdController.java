package com.controller;

import com.BazarakFrontendApplication;
import com.model.Advertisement;
import com.model.Category;
import com.model.City;
import com.model.User;
import com.service.AdService;
import com.service.CategoryService;
import com.service.CityService;
import com.util.NavigationUtil;
import com.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.util.List;
import java.util.Map;

public class CreateAdController {

    // ============================================
    // FXML FIELDS
    // ============================================

    @FXML
    private TextField titleField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextField priceField;

    @FXML
    private ComboBox<Category> categoryCombo;

    @FXML
    private ComboBox<City> cityCombo;

    @FXML
    private Label errorLabel;

    @FXML
    private ComboBox<Category> subCategoryCombo;

    private ObservableList<Category> rootCategories = FXCollections.observableArrayList();
    private ObservableList<Category> subCategories = FXCollections.observableArrayList();

    // ============================================
    // INITIALIZE
    // ============================================

    @FXML
    private void initialize() {
        loadCategories();
        loadCities();
    }

    // ============================================
    // LOAD DATA FROM BACKEND
    // ============================================

    private void loadCategories() {
        try {
            // Load all categories for parent combo
            List<Category> allCategories = CategoryService.getAllCategories();
            categoryCombo.setItems(FXCollections.observableArrayList(allCategories));
            categoryCombo.setPromptText("Select Category");

            // Load root categories for sub-category selection
            List<Category> roots = CategoryService.getRootCategories();
            rootCategories.setAll(roots);
            subCategoryCombo.setItems(rootCategories);
            subCategoryCombo.setPromptText("Select Sub-Category");

            // Add listener for sub-category selection
            subCategoryCombo.setOnAction(e -> {
                Category selected = subCategoryCombo.getValue();
                if (selected != null) {
                    try {
                        List<Category> subs = CategoryService.getSubCategories(selected.getId());
                        subCategories.setAll(subs);
                        // You could show sub-categories in another combo or list
                    } catch (Exception ex) {
                        errorLabel.setText("Failed to load sub-categories: " + ex.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            errorLabel.setText("Failed to load categories: " + e.getMessage());
        }
    }

    private void loadCities() {
        try {
            List<City> cities = CityService.getAllCities();
            cityCombo.setItems(FXCollections.observableArrayList(cities));
            cityCombo.setPromptText("Select City");
        } catch (Exception e) {
            errorLabel.setText("Failed to load cities: " + e.getMessage());
        }
    }

    // ============================================
    // HANDLE CREATE AD
    // ============================================

    @FXML
    private void handleCreateAd() {
        // Get input values
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        String priceText = priceField.getText().trim();
        Category selectedCategory = categoryCombo.getValue();
        City selectedCity = cityCombo.getValue();

        // Validate fields
        if (title.isEmpty() || description.isEmpty() || priceText.isEmpty()) {
            errorLabel.setText("Please fill in all required fields");
            return;
        }

        if (selectedCategory == null) {
            errorLabel.setText("Please select a category");
            return;
        }

        if (selectedCity == null) {
            errorLabel.setText("Please select a city");
            return;
        }

        // Validate price
        long price;
        try {
            String cleanPrice = priceText.replace(",", "");
            price = Long.parseLong(cleanPrice);
            if (price <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Price must be a positive number");
            return;
        }

        try {
            // Get current user
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser == null) {
                errorLabel.setText("You must be logged in to create an ad");
                return;
            }

            // Build ad object
            Advertisement ad = new Advertisement();
            ad.setTitle(title);
            ad.setDescription(description);
            ad.setPrice(price);
            ad.setCategory(selectedCategory);
            ad.setCity(selectedCity);

            // Send to backend
            Map<String, Object> result =  AdService.createAd(ad);

            // Show success message
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText((String)result.get("message"));
            alert.showAndWait();

            // Go back to home page
            BazarakFrontendApplication.showHomePage();

        } catch (Exception e) {
            errorLabel.setText("Failed to create ad: " + e.getMessage());
        }
    }

    // ============================================
    // NAVIGATION
    // ============================================

    @FXML
    private void cancel() {
        NavigationUtil.goToHome();
    }

}
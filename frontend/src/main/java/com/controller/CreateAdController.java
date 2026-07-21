package com.controller;

import com.BazarakFrontendApplication;
import com.model.*;
import com.service.AdService;
import com.service.CategoryService;
import com.service.CityService;
import com.util.NavigationUtil;
import com.util.SessionManager;
import com.view.ShowErrorDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;
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
    private ComboBox<Category> subCategoryCombo;

    @FXML
    private VBox specificationsContainer;

    private Map<Long, String> specValues = new HashMap<>();
    private List<CategorySpecification> currentSpecs = new ArrayList<>();

    private ObservableList<Category> rootCategories = FXCollections.observableArrayList();
    private ObservableList<Category> subCategories = FXCollections.observableArrayList();

    // ============================================
    // INITIALIZE
    // ============================================

    @FXML
    private void initialize() {
        loadCategories();
        loadCities();

        // Listener for category selection
        categoryCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadSpecificationsForCategory(newVal);
            } else {
                specificationsContainer.getChildren().clear();
                specValues.clear();
            }
        });

    }

    // Method to load specifications
    private void loadSpecificationsForCategory(Category category) {
        try {
            specificationsContainer.getChildren().clear();
            specValues.clear();

            List<CategorySpecification> specs = CategoryService.getCategorySpecifications(category.getId());
            currentSpecs = specs;

            if (specs.isEmpty()) {
                Label noSpecLabel = new Label("No specific specifications for this category.");
                noSpecLabel.setStyle("-fx-text-fill: #6c757d;");
                specificationsContainer.getChildren().add(noSpecLabel);
                return;
            }

            for (CategorySpecification spec : specs) {
                Label label = new Label(spec.getName() + (spec.isRequired() ? " *" : ""));
                label.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");
                Node input = createInputForSpec(spec);
                specificationsContainer.getChildren().addAll(label, input);
            }

        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog("Specifications Error", "Failed to load specifications", e.getMessage(), "ERROR");
        }
    }

    private Node createInputForSpec(CategorySpecification spec) {
        switch (spec.getType()) {
            case "TEXT":
                TextField textField = new TextField();
                textField.setPromptText("Enter " + spec.getName());
                textField.textProperty().addListener((obs, old, newVal) -> {
                    specValues.put(spec.getId(), newVal);
                });
                return textField;
            case "NUMBER":
                TextField numberField = new TextField();
                numberField.setPromptText("Enter " + spec.getName());
                numberField.textProperty().addListener((obs, old, newVal) -> {
                    if (!newVal.matches("\\d*(\\.\\d*)?")) {
                        numberField.setText(newVal.replaceAll("[^\\d.]", ""));
                    }
                    specValues.put(spec.getId(), numberField.getText());
                });
                return numberField;
            case "BOOLEAN":
                CheckBox checkBox = new CheckBox();
                checkBox.selectedProperty().addListener((obs, old, newVal) -> {
                    specValues.put(spec.getId(), newVal ? "true" : "false");
                });
                return checkBox;
            case "DROPDOWN":
                ComboBox<String> combo = new ComboBox<>();
                if (spec.getOptions() != null && !spec.getOptions().isEmpty()) {
                    combo.getItems().addAll(spec.getOptions().split(","));
                }
                combo.setPromptText("Select " + spec.getName());
                combo.valueProperty().addListener((obs, old, newVal) -> {
                    specValues.put(spec.getId(), newVal);
                });
                return combo;
            default:
                return new Label("Unsupported type");
        }
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
                        ShowErrorDialog.showErrorDialog(
                                "Category Error",
                                "Failed to load sub-categories",
                                ex.getMessage(),
                                "ERROR"
                        );
                    }
                }
            });

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
            cityCombo.setPromptText("Select City");
        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "City Error",
                    "Failed to load cities",
                    e.getMessage(),
                    "ERROR"
            );
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
            ShowErrorDialog.showErrorDialog(
                    "Validation Error",
                    "Incomplete information",
                    "Please fill in all required fields.",
                    "WARNING"
            );
            return;
        }

        if (selectedCategory == null) {
            ShowErrorDialog.showErrorDialog(
                    "Validation Error",
                    "Category not selected",
                    "Please select a category.",
                    "WARNING"
            );
            return;
        }

        if (selectedCity == null) {
            ShowErrorDialog.showErrorDialog(
                    "Validation Error",
                    "City not selected",
                    "Please select a city.",
                    "WARNING"
            );
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
            ShowErrorDialog.showErrorDialog(
                    "Validation Error",
                    "Invalid price",
                    "Price must be a positive number.",
                    "WARNING"
            );
            return;
        }

        try {
            // Get current user
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser == null) {
                ShowErrorDialog.showErrorDialog(
                        "Authentication Error",
                        "Login required",
                        "You must be logged in to create an ad.",
                        "ERROR"
                );
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
            ShowErrorDialog.showErrorDialog(
                    "Success",
                    null,
                    (String) result.get("message"),
                    "INFORMATION"
            );

            // Go back to home page
            BazarakFrontendApplication.showHomePage();

        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Create Advertisement Error",
                    "Failed to create advertisement",
                    e.getMessage(),
                    "ERROR"
            );
        }
    }

    // ============================================
    // NAVIGATION
    // ============================================

    @FXML
    private void cancel() {
        NavigationUtil.goBack();
    }
    @FXML
    private void goBack() {
        NavigationUtil.goBack();
    }

}
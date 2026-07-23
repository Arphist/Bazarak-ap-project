package com.controller;

import com.BazarakFrontendApplication;
import com.model.*;
import com.service.AdService;
import com.service.CategoryService;
import com.service.CityService;
import com.service.ImageService;
import com.util.NavigationUtil;
import com.util.SessionManager;
import com.view.ShowErrorDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

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
    private ComboBox<Category> categoryCombo;  // Now shows hierarchy and selects leaf

    @FXML
    private ComboBox<City> cityCombo;

    @FXML
    private VBox specificationsContainer;

    @FXML
    private Label errorLabel;

    @FXML
    private ListView<File> imageListView;

    private ObservableList<File> selectedImages = FXCollections.observableArrayList();

    private Map<Long, String> specValues = new HashMap<>();
    private List<CategorySpecification> currentSpecs = new ArrayList<>();

    // ============================================
    // INITIALIZE
    // ============================================

    @FXML
    private void initialize() {
        loadCategories();
        loadCities();

        // Setup image list view
        imageListView.setItems(selectedImages);
        imageListView.setPlaceholder(new Label("No images selected"));
        imageListView.setCellFactory(lv -> new ListCell<File>() {
            @Override
            protected void updateItem(File file, boolean empty) {
                super.updateItem(file, empty);
                if (empty || file == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(file.getName());
                    Button removeBtn = new Button("✕");
                    removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-cursor: hand;");
                    removeBtn.setOnAction(e -> selectedImages.remove(file));
                    setGraphic(removeBtn);
                    setContentDisplay(ContentDisplay.RIGHT);
                }
            }
        });

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

    // ============================================
    // LOAD DATA FROM BACKEND
    // ============================================

    private void loadCategories() {
        try {
            List<Category> allCategories = CategoryService.getAllCategories();

            // Build a map for quick lookup of parent names
            Map<Long, Category> categoryMap = allCategories.stream()
                    .collect(Collectors.toMap(Category::getId, c -> c));

            // ✅ Filter only leaf categories (categories with no subcategories)
            List<Category> leafCategories = allCategories.stream()
                    .filter(cat -> !hasSubcategories(cat.getId(), categoryMap))
                    .collect(Collectors.toList());

            ObservableList<Category> displayList = FXCollections.observableArrayList(leafCategories);

            // Set the combo items
            categoryCombo.setItems(displayList);
            categoryCombo.setPromptText("Select Category");

            // Set cell factory to show the hierarchy
            categoryCombo.setCellFactory(lv -> new ListCell<Category>() {
                @Override
                protected void updateItem(Category category, boolean empty) {
                    super.updateItem(category, empty);
                    if (empty || category == null) {
                        setText(null);
                    } else {
                        setText(buildCategoryPath(category, categoryMap));
                    }
                }
            });

            // Set button cell to show the selected category's path
            categoryCombo.setButtonCell(new ListCell<Category>() {
                @Override
                protected void updateItem(Category category, boolean empty) {
                    super.updateItem(category, empty);
                    if (empty || category == null) {
                        setText(categoryCombo.getPromptText());
                    } else {
                        setText(buildCategoryPath(category, categoryMap));
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

    /**
     * Checks if a category has subcategories (children).
     */
    private boolean hasSubcategories(Long categoryId, Map<Long, Category> categoryMap) {
        for (Category cat : categoryMap.values()) {
            if (cat.getParentId() != null && cat.getParentId().equals(categoryId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Builds a full hierarchical path for a category (e.g., "Electronics - Phones - Smartphones").
     */
    private String buildCategoryPath(Category cat, Map<Long, Category> categoryMap) {
        List<String> path = new ArrayList<>();
        Category current = cat;
        while (current != null) {
            path.add(current.getName());
            if (current.getParentId() == null) {
                break;
            }
            current = categoryMap.get(current.getParentId());
        }
        // Reverse to show from root to leaf
        Collections.reverse(path);
        return String.join(" - ", path);
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
                textField.textProperty().addListener((obs, old, newVal) -> specValues.put(spec.getId(), newVal));
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
                checkBox.selectedProperty().addListener((obs, old, newVal) -> specValues.put(spec.getId(), newVal ? "true" : "false"));
                return checkBox;
            case "DROPDOWN":
                ComboBox<String> combo = new ComboBox<>();
                if (spec.getOptions() != null && !spec.getOptions().isEmpty()) {
                    combo.getItems().addAll(spec.getOptions().split(","));
                }
                combo.setPromptText("Select " + spec.getName());
                combo.valueProperty().addListener((obs, old, newVal) -> specValues.put(spec.getId(), newVal));
                return combo;
            default:
                return new Label("Unsupported type");
        }
    }

    // ============================================
    // HANDLE CREATE AD
    // ============================================

    @FXML
    private void handleCreateAd() {
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        String priceText = priceField.getText().trim();
        Category selectedCategory = categoryCombo.getValue();
        City selectedCity = cityCombo.getValue();

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

            Advertisement ad = new Advertisement();
            ad.setTitle(title);
            ad.setDescription(description);
            ad.setPrice(price);
            ad.setCategory(selectedCategory);  // selectedCategory is the leaf
            ad.setCity(selectedCity);

            Map<String, Object> result = AdService.createAd(ad, specValues);
            Long adId = ((Number) result.get("id")).longValue();

            // Upload images
            if (!selectedImages.isEmpty()) {
                boolean isFirst = true;
                for (File file : selectedImages) {
                    try {
                        ImageService.uploadImage(adId, file, isFirst);
                        isFirst = false;
                    } catch (Exception e) {
                        System.err.println("Failed to upload image: " + file.getName() + " - " + e.getMessage());
                    }
                }
            }

            ShowErrorDialog.showErrorDialog(
                    "Success",
                    null,
                    (String) result.get("message"),
                    "INFORMATION"
            );

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
    // IMAGE UPLOAD METHODS
    // ============================================

    @FXML
    private void handleSelectImages() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Images");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp", "*.webp")
        );
        List<File> files = fileChooser.showOpenMultipleDialog(new Stage());
        if (files != null && !files.isEmpty()) {
            selectedImages.addAll(files);
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
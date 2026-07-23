package com.controller;

import com.BazarakFrontendApplication;
import com.model.*;
import com.service.AdService;
import com.service.CategoryService;
import com.service.CityService;
import com.service.ImageService;
import com.util.DataHolder;
import com.util.NavigationUtil;
import com.util.SessionManager;
import com.view.ShowErrorDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.*;

public class UpdateAdController {

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

    @FXML
    private Label errorLabel;

    @FXML
    private ListView<File> imageListView;

    private ObservableList<File> selectedImages = FXCollections.observableArrayList();

    private Map<Long, String> specValues = new HashMap<>();
    private List<CategorySpecification> currentSpecs = new ArrayList<>();

    private Advertisement editingAd;

    @FXML
    private ListView<Image> existingImagesListView;

    private ObservableList<Image> existingImages = FXCollections.observableArrayList();

    // ============================================
    // INITIALIZE
    // ============================================

    @FXML
    private void initialize() {
        // Get ad ID from DataHolder
        Long adId = DataHolder.getSelectedAdId();
        if (adId == null) {
            ShowErrorDialog.showErrorDialog(
                    "Error",
                    "No ad selected",
                    "Please select an ad to edit.",
                    "ERROR"
            );
            return;
        }

        // Load ad from backend
        try {
            Map<String, Object> result = AdService.getAdById(adId);
            editingAd = (Advertisement) result.get("ad");
            DataHolder.clearSelectedAdId();
        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Error",
                    "Failed to load ad",
                    e.getMessage(),
                    "ERROR"
            );
            return;
        }

        loadCategories();
        loadCities();
        loadAdData();

        // Setup image list view
        imageListView.setItems(selectedImages);
        imageListView.setPlaceholder(new Label("No new images selected"));
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
                    removeBtn.setOnAction(e -> {
                        selectedImages.remove(file);
                    });
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
    // LOAD AD DATA
    // ============================================

    private void loadAdData() {
        // Populate basic fields
        titleField.setText(editingAd.getTitle());
        descriptionArea.setText(editingAd.getDescription());
        priceField.setText(String.valueOf(editingAd.getPrice()));
        categoryCombo.setValue(editingAd.getCategory());
        cityCombo.setValue(editingAd.getCity());

        // Load existing specifications
        loadExistingSpecifications();

        // Load existing images
        loadExistingImages();
    }

    // ============================================
    // LOAD EXISTING SPECIFICATIONS
    // ============================================

    private void loadExistingSpecifications() {
        if (editingAd.getSpecificationDetails() != null) {
            for (AdvertisementSpecification spec : editingAd.getSpecificationDetails()) {
                specValues.put(spec.getSpecification().getId(), spec.getValue());
            }
        }
    }

    // ============================================
    // LOAD EXISTING IMAGES
    // ============================================

    private void loadExistingImages() {
        try {
            List<Image> images = ImageService.getImagesByAd(editingAd.getId());
            existingImages.clear();
            existingImages.addAll(images);
            existingImagesListView.setItems(existingImages);

            if (images.isEmpty()) {
                existingImagesListView.setPlaceholder(new Label("No images for this ad"));
            } else {
                existingImagesListView.setPlaceholder(null);
            }

            // Custom cell for existing images
            existingImagesListView.setCellFactory(lv -> new ListCell<Image>() {
                @Override
                protected void updateItem(Image img, boolean empty) {
                    super.updateItem(img, empty);
                    if (empty || img == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        try {
                            String url = ImageService.getImageUrl(img.getId());
                            javafx.scene.image.Image fxImage = new javafx.scene.image.Image(url, true);
                            ImageView thumbView = new ImageView(fxImage);
                            thumbView.setFitHeight(50);
                            thumbView.setFitWidth(50);
                            thumbView.setPreserveRatio(true);

                            Button deleteBtn = new Button("✕");
                            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-cursor: hand;");
                            deleteBtn.setOnAction(e -> {
                                deleteExistingImage(img);
                            });

                            HBox cellBox = new HBox(10);
                            cellBox.getChildren().addAll(thumbView, deleteBtn);
                            setGraphic(cellBox);
                        } catch (Exception e) {
                            setText("Error loading image");
                        }
                    }
                }
            });

        } catch (Exception e) {
            System.err.println("Failed to load existing images: " + e.getMessage());
        }
    }

    private void deleteExistingImage(Image image) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Image");
        confirm.setHeaderText("Delete image?");
        confirm.setContentText("Are you sure you want to delete this image?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                ImageService.deleteImage(image.getId());
                existingImages.remove(image);
                ShowErrorDialog.showErrorDialog(
                        "Success",
                        null,
                        "Image deleted successfully.",
                        "INFORMATION"
                );
            } catch (Exception e) {
                ShowErrorDialog.showErrorDialog(
                        "Error",
                        "Failed to delete image",
                        e.getMessage(),
                        "ERROR"
                );
            }
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
    // LOAD DATA FROM BACKEND
    // ============================================

    private void loadCategories() {
        try {
            List<Category> allCategories = CategoryService.getAllCategories();
            categoryCombo.setItems(FXCollections.observableArrayList(allCategories));
            categoryCombo.setPromptText("Select Category");

            List<Category> roots = CategoryService.getRootCategories();
            subCategoryCombo.setItems(FXCollections.observableArrayList(roots));
            subCategoryCombo.setPromptText("Select Sub-Category");

            subCategoryCombo.setOnAction(e -> {
                Category selected = subCategoryCombo.getValue();
                if (selected != null) {
                    try {
                        CategoryService.getSubCategories(selected.getId());
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
    // HANDLE UPDATE AD
    // ============================================

    @FXML
    private void handleUpdateAd() {
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
                        "You must be logged in to update an ad.",
                        "ERROR"
                );
                return;
            }

            // Build updated ad object
            Advertisement ad = new Advertisement();
            ad.setId(editingAd.getId());
            ad.setTitle(title);
            ad.setDescription(description);
            ad.setPrice(price);
            ad.setCategory(selectedCategory);
            ad.setCity(selectedCity);

            // Update ad
            Map<String, Object> result = AdService.updateAd(ad, specValues);

            // Upload new images if any
            if (!selectedImages.isEmpty()) {
                boolean isFirst = true;
                for (File file : selectedImages) {
                    try {
                        ImageService.uploadImage(ad.getId(), file, isFirst);
                        isFirst = false;
                    } catch (Exception e) {
                        System.err.println("Failed to upload image: " + file.getName() + " - " + e.getMessage());
                    }
                }
            }

            ShowErrorDialog.showErrorDialog(
                    "Success",
                    null,
                    "Ad updated successfully!",
                    "INFORMATION"
            );

            BazarakFrontendApplication.showHomePage();

        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Update Error",
                    "Failed to update ad",
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
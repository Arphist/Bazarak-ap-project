package com.controller;

import com.model.*;
import com.service.AdminService;
import com.service.AdminService;
import com.service.CategoryService;
import com.service.CityService;
import com.util.DataHolder;
import com.util.HttpClientUtil;
import com.util.NavigationUtil;
import com.view.AdCell;
import com.view.ShowErrorDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDashboard {
    @FXML
    private TextField cityProvinceFilterField;

    @FXML
    private ListView<Advertisement> recentAdsListView;
    @FXML
    private ListView<String> adsByCategoryListView;
    @FXML
    private ListView<String> adsByCityListView;

    @FXML
    private VBox dashboardStatsView;
    @FXML
    private VBox citiesView;
    @FXML
    private VBox categoriesView;
    @FXML
    private VBox specificationsView;

    // FXML FIELDS - SPECIFICATIONS
    @FXML
    private ComboBox<Category> specCategoryCombo;

    @FXML
    private TextField specNameField;

    @FXML
    private ComboBox<String> specTypeCombo;

    @FXML
    private TextField specOptionsField;

    @FXML
    private CheckBox specRequiredCheck;

    @FXML
    private ListView<CategorySpecification> specificationsListView;

    @FXML
    private Label specErrorLabel;

    private ObservableList<CategorySpecification> specifications = FXCollections.observableArrayList();
    private CategorySpecification selectedSpecification;

    // FXML FIELDS - PENDING ADS

    @FXML
    private ListView<Advertisement> pendingAdsListView;

    @FXML
    private Label pendingCountLabel;

    @FXML
    private ComboBox<String> pendingSortByCombo;

    @FXML
    private ComboBox<String> pendingSortOrderCombo;

    // FXML FIELDS - ACTIVE ADS

    @FXML
    private ListView<Advertisement> activeAdsListView;

    @FXML
    private Label activeCountLabel;

    @FXML
    private ComboBox<String> activeSortByCombo;

    @FXML
    private ComboBox<String> activeSortOrderCombo;

    // FXML FIELDS - REJECTED ADS

    @FXML
    private ListView<Advertisement> rejectedAdsListView;

    @FXML
    private Label rejectedCountLabel;

    @FXML
    private ComboBox<String> rejectedSortByCombo;

    @FXML
    private ComboBox<String> rejectedSortOrderCombo;

    // FXML FIELDS - SOLD ADS

    @FXML
    private ListView<Advertisement> soldAdsListView;

    @FXML
    private Label soldCountLabel;

    @FXML
    private ComboBox<String> soldSortByCombo;

    @FXML
    private ComboBox<String> soldSortOrderCombo;

    // FXML FIELDS - DELETED ADS

    @FXML
    private ListView<Advertisement> deletedAdsListView;

    @FXML
    private Label deletedCountLabel;

    @FXML
    private ComboBox<String> deletedSortByCombo;

    @FXML
    private ComboBox<String> deletedSortOrderCombo;

    // FXML FIELDS - USERS

    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, Long> userIdCol;

    @FXML
    private TableColumn<User, String> userUsernameCol;

    @FXML
    private TableColumn<User, String> userFullNameCol;

    @FXML
    private TableColumn<User, String> userEmailCol;

    @FXML
    private TableColumn<User, String> userStatusCol;

    @FXML
    private TableColumn<User, String> userRoleCol;

    @FXML
    private Label usersCountLabel;

    @FXML
    private Label totalAdsLabel;

    // FXML FIELDS - CITIES
    @FXML
    private ListView<City> cityListView;

    @FXML
    private TextField cityNameField;

    @FXML
    private TextField cityProvinceField;

    @FXML
    private TextField citySearchField;


    // FXML FIELDS - CATEGORIES

    @FXML
    private ListView<Category> categoryListView;

    @FXML
    private TextField categoryNameField;

    @FXML
    private TextField categorySearchField;

    @FXML
    private TextField categoryDescriptionField;

    @FXML
    private ComboBox<Category> categoryParentCombo;

    // DATA

    private ObservableList<Advertisement> pendingAds = FXCollections.observableArrayList();
    private ObservableList<Advertisement> activeAds = FXCollections.observableArrayList();
    private ObservableList<Advertisement> rejectedAds = FXCollections.observableArrayList();
    private ObservableList<Advertisement> soldAds = FXCollections.observableArrayList();
    private ObservableList<Advertisement> deletedAds = FXCollections.observableArrayList();
    private ObservableList<User> users = FXCollections.observableArrayList();
    private ObservableList<City> cities = FXCollections.observableArrayList();
    private ObservableList<Category> categories = FXCollections.observableArrayList();

    // INITIALIZE

    @FXML
    private void initialize() {
        // Show only the dashboard stats by default
        showDashboard();

        setupAdListView(pendingAdsListView);
        setupAdListView(activeAdsListView);
        setupAdListView(rejectedAdsListView);
        setupAdListView(soldAdsListView);
        setupAdListView(deletedAdsListView);


        if (pendingAds.isEmpty()) {
            pendingAdsListView.setPlaceholder(new Label("No pending ads found"));
        } else {
            pendingAdsListView.setPlaceholder(null);
        }
        if (activeAds.isEmpty()) {
            activeAdsListView.setPlaceholder(new Label("No active ads found"));
        } else {
            activeAdsListView.setPlaceholder(null);
        }
        if (rejectedAds.isEmpty()) {
            rejectedAdsListView.setPlaceholder(new Label("No rejected ads found"));
        } else {
            rejectedAdsListView.setPlaceholder(null);
        }
        if (soldAds.isEmpty()) {
            soldAdsListView.setPlaceholder(new Label("No sold ads found"));
        } else {
            soldAdsListView.setPlaceholder(null);
        }
        if (deletedAds.isEmpty()) {
            deletedAdsListView.setPlaceholder(new Label("No deleted ads found"));
        } else {
            deletedAdsListView.setPlaceholder(null);
        }


        setupUserTable();

        // Setup sort combo boxes
        setupSortComboBoxes();

        loadAllData();

        // Load categories for spec management
        loadSpecCategories();

        // Populate type combo for specifications
        specTypeCombo.setItems(FXCollections.observableArrayList("TEXT", "NUMBER", "BOOLEAN", "DROPDOWN"));
        specTypeCombo.setPromptText("Type");

        // Setup specifications list
        specificationsListView.setItems(specifications);
        specificationsListView.setCellFactory(lv -> new ListCell<CategorySpecification>() {
            @Override
            protected void updateItem(CategorySpecification spec, boolean empty) {
                super.updateItem(spec, empty);
                if (empty || spec == null) {
                    setText(null);
                } else {
                    setText(spec.getName() + " (" + spec.getType() + ")" + (spec.isRequired() ? " *" : ""));
                }
            }
        });
        specificationsListView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            selectedSpecification = newVal;
            if (newVal != null) {
                // Populate edit fields
                specNameField.setText(newVal.getName());
                specTypeCombo.setValue(newVal.getType());
                specOptionsField.setText(newVal.getOptions());
                specRequiredCheck.setSelected(newVal.isRequired());
            }
        });
        if (specifications.isEmpty()) {
            specificationsListView.setPlaceholder(new Label("No specification found"));
        } else {
            specificationsListView.setPlaceholder(null);
        }

        // Populate type combo
        specTypeCombo.setItems(FXCollections.observableArrayList("TEXT", "NUMBER", "BOOLEAN", "DROPDOWN"));

        // Setup recent ads list view
        setupAdListView(recentAdsListView);  // reuse the same cell factory
        recentAdsListView.setPlaceholder(new Label("No recent ads"));

        // Setup recent ads list view with text-only cells (no images)
        recentAdsListView.setCellFactory(lv -> new ListCell<Advertisement>() {
            @Override
            protected void updateItem(Advertisement ad, boolean empty) {
                super.updateItem(ad, empty);
                if (empty || ad == null) {
                    setText(null);
                    setStyle("");
                } else {
                    String text = ad.getTitle() + " - " + ad.getPrice() + " T";
                    if (ad.getOwner() != null) {
                        text += " (by " + ad.getOwner().getUsername() + ")";
                    }
                    if (ad.getCreatedAt() != null) {
                        text += " | " + ad.getCreatedAt().toLocalDate().toString();
                    }
                    setText(text);
                    setStyle("-fx-padding: 6 12; -fx-border-color: #333333; -fx-border-width: 0 0 1 0; -fx-background-color: transparent;");
                }
            }
        });

        // Setup category and city list views
        setupSimpleListView(adsByCategoryListView);
        setupSimpleListView(adsByCityListView);

        usersTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                User selected = usersTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openUserAds(selected);
                }
            }
        });
    }

    private void openUserAds(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/my-ads.fxml"));
            Parent root = loader.load();
            UserAdController controller = loader.getController();

            controller.setTargetUser(user);
            controller.setHeaderText("Ads of " + user.getUsername());

            //  Load all data after setting the target
            controller.loadAllData();  // ← This will use the targetUser now

            Stage stage = new Stage();
            stage.setTitle("Ads of " + user.getUsername());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            ShowErrorDialog.showErrorDialog("Error", "Failed to open user ads", e.getMessage(), "ERROR");
        }
    }
    /**
     * Helper to set a simple string list view.
     */
    private void setupSimpleListView(ListView<String> listView) {
        listView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                }
            }
        });
    }

    @FXML
    private void showDashboard() {
        setVisibleView(dashboardStatsView);
    }

    @FXML
    private void showCities() {
        setVisibleView(citiesView);
        // Refresh city list if needed
        loadCities();
    }

    @FXML
    private void showCategories() {
        setVisibleView(categoriesView);
        // Refresh category list if needed
        loadCategories();
    }

    @FXML
    private void showSpecifications() {
        setVisibleView(specificationsView);
        // Load categories for the spec dropdown
        loadSpecCategories();
        // Optionally clear previous selection
        specCategoryCombo.setValue(null);
        specifications.clear();
    }

    private void setVisibleView(VBox viewToShow) {
        // Hide all views
        dashboardStatsView.setVisible(false);
        dashboardStatsView.setManaged(false);
        citiesView.setVisible(false);
        citiesView.setManaged(false);
        categoriesView.setVisible(false);
        categoriesView.setManaged(false);
        specificationsView.setVisible(false);
        specificationsView.setManaged(false);

        // Show the selected one
        viewToShow.setVisible(true);
        viewToShow.setManaged(true);
    }

    private void loadSpecCategories() {
        try {
            List<Category> categories = CategoryService.getAllCategories();
            ObservableList<Category> categoryList = FXCollections.observableArrayList(categories);
            specCategoryCombo.setItems(categoryList);
            specCategoryCombo.setPromptText("Select a category");

            // Set cell factory to show category name
            specCategoryCombo.setCellFactory(lv -> new ListCell<Category>() {
                @Override
                protected void updateItem(Category category, boolean empty) {
                    super.updateItem(category, empty);
                    if (empty || category == null) {
                        setText(null);
                    } else {
                        setText(category.getName());
                    }
                }
            });

            //  Set button cell to show category name when selected
            specCategoryCombo.setButtonCell(new ListCell<Category>() {
                @Override
                protected void updateItem(Category category, boolean empty) {
                    super.updateItem(category, empty);
                    if (empty || category == null) {
                        setText(null);
                    } else {
                        setText(category.getName());
                    }
                }
            });

        } catch (Exception e) {
            specErrorLabel.setText("Failed to load categories: " + e.getMessage());
        }
    }

    // SPECIFICATION MANAGEMENT

    @FXML
    private void onSpecCategorySelected() {
        Category selected = specCategoryCombo.getValue();
        if (selected == null) {
            return;
        }
        loadSpecificationsForCategory(selected.getId());
    }

    private void loadSpecificationsForCategory(Long categoryId) {
        try {
            List<CategorySpecification> specs = CategoryService.getCategorySpecifications(categoryId);
            specifications.setAll(specs);
            specErrorLabel.setText("");
        } catch (Exception e) {
            specErrorLabel.setText("Failed to load specifications: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddSpecification() {
        Category selectedCategory = specCategoryCombo.getValue();
        if (selectedCategory == null) {
            specErrorLabel.setText("Please select a category first");
            return;
        }

        String name = specNameField.getText().trim();
        String type = specTypeCombo.getValue();
        String options = null;
        if(specOptionsField.getText()!=null){
             options = specOptionsField.getText().trim();
        }
        boolean required = specRequiredCheck.isSelected();

        if (name.isEmpty()) {
            specErrorLabel.setText("Specification name is required");
            return;
        }

        if (type == null || type.isEmpty()) {
            specErrorLabel.setText("Please select a type");
            return;
        }

        try {
            CategorySpecification spec = new CategorySpecification();
            spec.setName(name);
            spec.setType(type);
            spec.setOptions(options.isEmpty() ? null : options);
            spec.setRequired(required);

            CategorySpecification created = CategoryService.addSpecificationToCategory(selectedCategory.getId(), spec);
            loadSpecificationsForCategory(selectedCategory.getId());

            // Clear fields
            specNameField.clear();
            specOptionsField.clear();
            specRequiredCheck.setSelected(false);
            specTypeCombo.setValue(null);

            specErrorLabel.setText("Specification added successfully");

        } catch (Exception e) {
            specErrorLabel.setText("Failed to add specification: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditSpecification() {
        if (selectedSpecification == null) {
            specErrorLabel.setText("Please select a specification to edit");
            return;
        }

        Category selectedCategory = specCategoryCombo.getValue();
        if (selectedCategory == null) {
            specErrorLabel.setText("Please select a category");
            return;
        }

        String name = specNameField.getText().trim();
        String type = specTypeCombo.getValue();
        String options = specOptionsField.getText().trim();
        boolean required = specRequiredCheck.isSelected();

        if (name.isEmpty()) {
            specErrorLabel.setText("Specification name is required");
            return;
        }

        if (type == null || type.isEmpty()) {
            specErrorLabel.setText("Please select a type");
            return;
        }

        try {
            selectedSpecification.setName(name);
            selectedSpecification.setType(type);
            selectedSpecification.setOptions(options.isEmpty() ? null : options);
            selectedSpecification.setRequired(required);

            CategorySpecification updated = CategoryService.updateSpecification(selectedSpecification.getId(), selectedSpecification);
            loadSpecificationsForCategory(selectedCategory.getId());

            specErrorLabel.setText("Specification updated successfully");

        } catch (Exception e) {
            specErrorLabel.setText("Failed to update specification: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteSpecification() {
        if (selectedSpecification == null) {
            specErrorLabel.setText("Please select a specification to delete");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete specification?");
        confirm.setContentText("Are you sure you want to delete '" + selectedSpecification.getName() + "'?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                String result = CategoryService.deleteSpecification(selectedSpecification.getId());
                Category selectedCategory = specCategoryCombo.getValue();
                if (selectedCategory != null) {
                    loadSpecificationsForCategory(selectedCategory.getId());
                }
                specErrorLabel.setText(result);

                // Clear selection
                selectedSpecification = null;
                specNameField.clear();
                specOptionsField.clear();
                specRequiredCheck.setSelected(false);
                specTypeCombo.setValue(null);

            } catch (Exception e) {
                specErrorLabel.setText("Failed to delete specification: " + e.getMessage());
            }
        }
    }

    private void setupSortComboBoxes() {
        // Pending
        pendingSortByCombo.setItems(FXCollections.observableArrayList("created_at", "title", "price"));
        pendingSortByCombo.setValue("created_at");
        pendingSortOrderCombo.setItems(FXCollections.observableArrayList("asc", "desc"));
        pendingSortOrderCombo.setValue("desc");

        // Active
        activeSortByCombo.setItems(FXCollections.observableArrayList("created_at", "title", "price"));
        activeSortByCombo.setValue("created_at");
        activeSortOrderCombo.setItems(FXCollections.observableArrayList("asc", "desc"));
        activeSortOrderCombo.setValue("desc");

        // Rejected
        rejectedSortByCombo.setItems(FXCollections.observableArrayList("created_at", "title", "price"));
        rejectedSortByCombo.setValue("created_at");
        rejectedSortOrderCombo.setItems(FXCollections.observableArrayList("asc", "desc"));
        rejectedSortOrderCombo.setValue("desc");

        // Sold
        soldSortByCombo.setItems(FXCollections.observableArrayList("created_at", "title", "price"));
        soldSortByCombo.setValue("created_at");
        soldSortOrderCombo.setItems(FXCollections.observableArrayList("asc", "desc"));
        soldSortOrderCombo.setValue("desc");

        // Deleted
        deletedSortByCombo.setItems(FXCollections.observableArrayList("created_at", "title", "price"));
        deletedSortByCombo.setValue("created_at");
        deletedSortOrderCombo.setItems(FXCollections.observableArrayList("asc", "desc"));
        deletedSortOrderCombo.setValue("desc");
    }

    private void setupAdListView(ListView<Advertisement> listView) {
        listView.setCellFactory(lv -> new AdCell());
    }

    private void setupUserTable() {
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        userUsernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        userFullNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        userEmailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        userStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        userRoleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
    }

    private void setupSingleSortCombo(ComboBox<String> sortBy, ComboBox<String> sortOrder) {
        sortBy.setValue("created_at");
        sortOrder.setValue("desc");
    }

    // LOAD DATA

    private void loadAllData() {
        loadPendingAds();
        loadActiveAds();
        loadRejectedAds();
        loadSoldAds();
        loadDeletedAds();
        loadUsers();
        loadCategories();
        loadCities();
        loadDashboardStats();
    }

    private void loadPendingAds() {
        try {
            String sortBy = pendingSortByCombo.getValue();
            String sortOrder = pendingSortOrderCombo.getValue();
            List<Advertisement> ads = AdminService.getPendingAds(sortBy, sortOrder);
            pendingAds.setAll(ads);
            pendingAdsListView.setItems(pendingAds);
            pendingCountLabel.setText("Pending: " + pendingAds.size());
        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Pending Ads Error",
                    "Failed to load pending ads",
                    "There was a problem loading pending ads. Please try again later.",
                    "ERROR"
            );
        }
    }

    private void loadActiveAds() {
        try {
            String sortBy = activeSortByCombo.getValue();
            String sortOrder = activeSortOrderCombo.getValue();
            List<Advertisement> ads = AdminService.getAdsByStatus("ACCEPTED", sortBy, sortOrder);
            activeAds.setAll(ads);
            activeAdsListView.setItems(activeAds);
            activeCountLabel.setText("Active: " + activeAds.size());
        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Active Ads Error",
                    "Failed to load active ads",
                    "There was a problem loading active ads. Please try again later.",
                    "ERROR"
            );
        }
    }

    private void loadRejectedAds() {
        try {
            String sortBy = rejectedSortByCombo.getValue();
            String sortOrder = rejectedSortOrderCombo.getValue();
            List<Advertisement> ads = AdminService.getAdsByStatus("REJECTED", sortBy, sortOrder);
            rejectedAds.setAll(ads);
            rejectedAdsListView.setItems(rejectedAds);
            rejectedCountLabel.setText("Rejected: " + rejectedAds.size());
        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Rejected Ads Error",
                    "Failed to load rejected ads",
                    "There was a problem loading rejected ads. Please try again later.",
                    "ERROR"
            );
        }
    }

    private void loadSoldAds() {
        try {
            String sortBy = soldSortByCombo.getValue();
            String sortOrder = soldSortOrderCombo.getValue();
            List<Advertisement> ads = AdminService.getAdsByStatus("SOLD", sortBy, sortOrder);
            soldAds.setAll(ads);
            soldAdsListView.setItems(soldAds);
            soldCountLabel.setText("Sold: " + soldAds.size());
        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Sold Ads Error",
                    "Failed to load sold ads",
                    "There was a problem loading sold ads. Please try again later.",
                    "ERROR"
            );
        }
    }

    private void loadDeletedAds() {
        try {
            String sortBy = deletedSortByCombo.getValue();
            String sortOrder = deletedSortOrderCombo.getValue();
            List<Advertisement> ads = AdminService.getAdsByStatus("DELETED", sortBy, sortOrder);
            deletedAds.setAll(ads);
            deletedAdsListView.setItems(deletedAds);
            deletedCountLabel.setText("Deleted: " + deletedAds.size());
        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Deleted Ads Error",
                    "Failed to load deleted ads",
                    "There was a problem loading deleted ads. Please try again later.",
                    "ERROR"
            );
        }
    }

    private void loadUsers() {
        try {
            List<User> userList = AdminService.getAllUsers();
            users.setAll(userList);
            usersTable.setItems(users);
            usersCountLabel.setText("Total Users: " + users.size());
        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Load Users Error",
                    "Failed to load users",
                    "There was a problem loading users. Please try again later.",
                    "ERROR"
            );
        }
    }

    private void loadCities() {
        try {
            List<City> cities = CityService.getAllCities();
            cityListView.getItems().setAll(cities);
            if (cities.isEmpty()) {
                cityListView.setPlaceholder(new Label("No city found"));
            } else {
                cityListView.setPlaceholder(null);
            }
            cityListView.setCellFactory(lv -> new ListCell<City>() {
                @Override
                protected void updateItem(City city, boolean empty) {
                    super.updateItem(city, empty);
                    if (empty || city == null) {
                        setText(null);
                    } else {
                        String display = city.getName();
                        if (city.getProvince() != null && !city.getProvince().isEmpty()) {
                            display += " (" + city.getProvince() + ")";
                        }
                        setText(display);
                    }
                }
            });
        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Load Cities Error",
                    "Failed to load cities",
                    "There was a problem loading cities. Please try again later.",
                    "ERROR"
            );
        }
    }

    private void loadCategories() {
        try {
            List<Category> categoryList = CategoryService.getAllCategories();
            categories.setAll(categoryList);

            // Build hierarchy map for quick lookup
            Map<Long, Category> categoryMap = new HashMap<>();
            for (Category cat : categoryList) {
                categoryMap.put(cat.getId(), cat);
            }

            // Apply the cell factory
            categoryListView.setCellFactory(lv -> new ListCell<Category>() {
                @Override
                protected void updateItem(Category cat, boolean empty) {
                    super.updateItem(cat, empty);
                    if (empty || cat == null) {
                        setText(null);
                    } else {
                        // Build the full hierarchy path
                        String display = cat.getName();
                        List<String> parents = new ArrayList<>();
                        Long parentId = cat.getParentId();

                        // Traverse up the hierarchy
                        while (parentId != null) {
                            Category parent = categoryMap.get(parentId);
                            if (parent != null) {
                                parents.add(parent.getName());
                                parentId = parent.getParentId();
                            } else {
                                break;
                            }
                        }

                        // Build the full path from root to current
                        if (!parents.isEmpty()) {
                            // Reverse to show from root to child
                            StringBuilder hierarchy = new StringBuilder();
                            for (int i = parents.size() - 1; i >= 0; i--) {
                                hierarchy.append(parents.get(i)).append(" - ");
                            }
                            hierarchy.append(display);
                            setText(hierarchy.toString());
                        } else {
                            setText(display);
                        }
                    }
                }
            });

            categoryListView.setItems(categories);

            if (categories.isEmpty()) {
                categoryListView.setPlaceholder(new Label("No category found"));
            } else {
                categoryListView.setPlaceholder(null);
            }

            // Update parent combo
            updateCategoryParentCombo();

        } catch (Exception e) {
            System.err.println("❌ Error loading categories: " + e.getMessage());
            e.printStackTrace();
            ShowErrorDialog.showErrorDialog(
                    "Load Categories Error",
                    "Failed to load categories",
                    e.getMessage(),
                    "ERROR"
            );
        }
    }

    /**
     * Filters cities by the province entered in the province filter field.
     */
    @FXML
    private void filterCitiesByProvince() {
        String province = cityProvinceFilterField.getText().trim();
        if (province.isEmpty()) {
            ShowErrorDialog.showErrorDialog(
                    "Filter Error",
                    "Province is empty",
                    "Please enter a province name to filter.",
                    "WARNING"
            );
            return;
        }

        try {
            List<City> filteredCities = CityService.getCitiesByProvince(province);
            cityListView.getItems().setAll(filteredCities);
            if (filteredCities.isEmpty()) {
                cityListView.setPlaceholder(new Label("No cities found for province: " + province));
            } else {
                cityListView.setPlaceholder(null);
            }
        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Filter Error",
                    "Failed to filter cities by province",
                    e.getMessage(),
                    "ERROR"
            );
        }
    }

    /**
     * Resets the city list to show all cities (clears any filter).
     */
    @FXML
    private void resetCityFilter() {
        cityProvinceFilterField.clear();
        loadCities(); // reloads all cities
    }

    // SEARCH METHODS

    @FXML
    private void searchCities() {
        String keyword = citySearchField.getText().trim();
        try {
            List<City> results;
            if (keyword.isEmpty()) {
                results = CityService.getAllCities();
            } else {
                results = CityService.searchCities(keyword);
            }
            cityListView.getItems().setAll(results);
            if (results.isEmpty()) {
                ShowErrorDialog.showErrorDialog(
                        "Load City Error",
                        "No cities found",
                        "There was a problem loading cities. Please try again later.",
                        "WARNING"
                );
            }

        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Load City Error",
                    "No cities found",
                    "There was a problem loading cities. Please try again later.",
                    "ERROR"
            );
        }
    }

    @FXML
    private void searchCategories() {
        String keyword = categorySearchField.getText().trim();
        try {
            List<Category> results;
            if (keyword.isEmpty()) {
                results = CategoryService.getAllCategories();
            } else {
                results = CategoryService.searchCategories(keyword);
            }

            // Update the items
            categoryListView.getItems().setAll(results);

            // Build hierarchy map
            Map<Long, Category> categoryMap = new HashMap<>();
            for (Category cat : results) {
                categoryMap.put(cat.getId(), cat);
            }

            // Reapply the cell factory with hierarchy
            categoryListView.setCellFactory(lv -> new ListCell<Category>() {
                @Override
                protected void updateItem(Category cat, boolean empty) {
                    super.updateItem(cat, empty);
                    if (empty || cat == null) {
                        setText(null);
                    } else {
                        String display = cat.getName();
                        List<String> parents = new ArrayList<>();
                        Long parentId = cat.getParentId();

                        while (parentId != null) {
                            Category parent = categoryMap.get(parentId);
                            if (parent != null) {
                                parents.add(parent.getName());
                                parentId = parent.getParentId();
                            } else {
                                break;
                            }
                        }

                        if (!parents.isEmpty()) {
                            StringBuilder hierarchy = new StringBuilder();
                            for (int i = parents.size() - 1; i >= 0; i--) {
                                hierarchy.append(parents.get(i)).append(" - ");
                            }
                            hierarchy.append(display);
                            setText(hierarchy.toString());
                        } else {
                            setText(display);
                        }
                    }
                }
            });

            if (results.isEmpty()) {
                categoryListView.setPlaceholder(new Label("No categories found"));
            } else {
                categoryListView.setPlaceholder(null);
            }

        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Search Category Error",
                    "Failed to search categories",
                    e.getMessage(),
                    "ERROR"
            );
        }
    }

    private void applyCategoryCellFactory() {
        // Get the current category list
        List<Category> categoryList = categoryListView.getItems();

        // Build a map for quick lookup
        Map<Long, Category> categoryMap = new HashMap<>();
        for (Category cat : categoryList) {
            categoryMap.put(cat.getId(), cat);
        }

        // Apply the cell factory
        categoryListView.setCellFactory(lv -> new ListCell<Category>() {
            @Override
            protected void updateItem(Category cat, boolean empty) {
                super.updateItem(cat, empty);
                if (empty || cat == null) {
                    setText(null);
                } else {
                    // Build the hierarchy string
                    String display = cat.getName();

                    // Get parent name if exists
                    if (cat.getParentId() != null) {
                        Category parent = categoryMap.get(cat.getParentId());
                        if (parent != null) {
                            display += " - " + parent.getName();

                            // Get grandparent name if exists
                            if (parent.getParentId() != null) {
                                Category grandParent = categoryMap.get(parent.getParentId());
                                if (grandParent != null) {
                                    display += " - " + grandParent.getName();
                                }
                            }
                        }
                    }

                    setText(display);
                }
            }
        });
    }

    private void loadDashboardStats() {
        try {
            Map<String, Object> stats = AdminService.getDashboardStats();

            totalAdsLabel.setText("Total: " + stats.get("totalAds"));
            pendingCountLabel.setText("Pending: " + stats.get("pendingAds"));
            activeCountLabel.setText("Active: " + stats.get("activeAds"));

            // --- 1. Recent Ads --- (already working)
            Object recentAdsObj = stats.get("recentAds");
            if (recentAdsObj instanceof List) {
                List<?> rawList = (List<?>) recentAdsObj;
                List<Advertisement> recentAds = new ArrayList<>();
                for (Object item : rawList) {
                    Advertisement ad = HttpClientUtil.getObjectMapper().convertValue(item, Advertisement.class);
                    recentAds.add(ad);
                }
                if (!recentAds.isEmpty()) {
                    ObservableList<Advertisement> recentObs = FXCollections.observableArrayList(recentAds);
                    recentAdsListView.setItems(recentObs);
                } else {
                    recentAdsListView.setPlaceholder(new Label("No recent ads"));
                }
            } else {
                recentAdsListView.setPlaceholder(new Label("No recent ads"));
            }

            // --- 2. Ads by Category --- (FIXED for array data)
            Object categoryDataObj = stats.get("adsByCategory");
            if (categoryDataObj instanceof List) {
                List<?> rawCategoryList = (List<?>) categoryDataObj;
                ObservableList<String> categoryItems = FXCollections.observableArrayList();
                for (Object entry : rawCategoryList) {
                    if (entry instanceof List) {
                        List<?> entryList = (List<?>) entry;
                        if (entryList.size() >= 2) {
                            String category = (String) entryList.get(0);
                            Number count = (Number) entryList.get(1);
                            if (category != null && count != null) {
                                categoryItems.add(category + ": " + count);
                            }
                        }
                    }
                }
                if (!categoryItems.isEmpty()) {
                    adsByCategoryListView.setItems(categoryItems);
                } else {
                    adsByCategoryListView.setPlaceholder(new Label("No category data"));
                }
            } else {
                adsByCategoryListView.setPlaceholder(new Label("No category data"));
            }

            // --- 3. Ads by City --- (FIXED for array data)
            Object cityDataObj = stats.get("adsByCity");
            if (cityDataObj instanceof List) {
                List<?> rawCityList = (List<?>) cityDataObj;
                ObservableList<String> cityItems = FXCollections.observableArrayList();
                for (Object entry : rawCityList) {
                    if (entry instanceof List) {
                        List<?> entryList = (List<?>) entry;
                        if (entryList.size() >= 2) {
                            String city = (String) entryList.get(0);
                            Number count = (Number) entryList.get(1);
                            if (city != null && count != null) {
                                cityItems.add(city + ": " + count);
                            }
                        }
                    }
                }
                if (!cityItems.isEmpty()) {
                    adsByCityListView.setItems(cityItems);
                } else {
                    adsByCityListView.setPlaceholder(new Label("No city data"));
                }
            } else {
                adsByCityListView.setPlaceholder(new Label("No city data"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            ShowErrorDialog.showErrorDialog(
                    "Load Data Error",
                    "Failed to load statistics data.",
                    e.getMessage(),
                    "ERROR"
            );
        }
    }

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

    private void updateCategoryParentCombo() {
        // Clear and repopulate with ALL categories
        categoryParentCombo.getItems().clear();

        // Add categories to the combo (excluding the current one being edited if needed)
        for (Category cat : categories) {
            categoryParentCombo.getItems().add(cat);
        }

        categoryParentCombo.setPromptText("Parent (optional)");

        // Optional: Set cell factory to show names
        categoryParentCombo.setCellFactory(lv -> new ListCell<Category>() {
            @Override
            protected void updateItem(Category category, boolean empty) {
                super.updateItem(category, empty);
                setText(empty || category == null ? null : category.getName());
            }
        });

        categoryParentCombo.setButtonCell(new ListCell<Category>() {
            @Override
            protected void updateItem(Category category, boolean empty) {
                super.updateItem(category, empty);
                setText(empty || category == null ? null : category.getName());
            }
        });
    }

    // SORT ACTIONS

    @FXML
    private void applyPendingSort() {
        loadPendingAds();
    }

    @FXML
    private void applyActiveSort() {
        loadActiveAds();
    }

    @FXML
    private void applyRejectedSort() {
        loadRejectedAds();
    }

    @FXML
    private void applySoldSort() {
        loadSoldAds();
    }

    @FXML
    private void applyDeletedSort() {
        loadDeletedAds();
    }

    // ACTIONS

    @FXML
    private void approveAd() {
        Advertisement selected = pendingAdsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ShowErrorDialog.showErrorDialog(
                    "Selection Error",
                    "No ad selected",
                    "Please select an ad",
                    "ERROR"
            );
            return;
        }

        try {
            Map<String, Object> result = AdminService.approveAd(selected);
            loadPendingAds();
            loadActiveAds();

            ShowErrorDialog.showErrorDialog(
                    "Approve Ad Success",
                    (String) result.get("message"),
                    null,
                    "CONFIRMATION"
            );
        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Approve Ad Error",
                    "Failed to approve ad",
                    "There was a problem approving ad. Please try again later.",
                    "ERROR"
            );
        }
    }

    @FXML
    private void rejectAd() {
        Advertisement selected = pendingAdsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ShowErrorDialog.showErrorDialog(
                    "Selection Error",
                    "No ad selected",
                    "Please select an ad",
                    "ERROR"
            );
            return;
        }

        try {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Reject Ad");
            dialog.setHeaderText("Enter rejection reason:");
            dialog.setContentText("Reason:");

            String reason = dialog.showAndWait().orElse("No reason provided");
            selected.setRejectionReason(reason);
            Map<String, Object> result = AdminService.rejectAd(selected);
            loadPendingAds();
            loadRejectedAds();

            ShowErrorDialog.showErrorDialog(
                    "Reject Ad Success",
                    (String) result.get("message"),
                    null,
                    "CONFIRMATION"
            );
        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Reject Ad Error",
                    "Failed to reject ad",
                    "There was a problem rejecting ad. Please try again later.",
                    "ERROR"
            );
        }
    }

    @FXML
    private void deleteAd() {
        ListView<Advertisement> currentListView = getCurrentListView();
        Advertisement selected = currentListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ShowErrorDialog.showErrorDialog(
                    "Selection Error",
                    "No ad selected",
                    "Please select an ad",
                    "ERROR"
            );
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete advertisement?");
        confirm.setContentText("Are you sure you want to delete '" + selected.getTitle() + "'?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                AdminService.deleteAd(selected);
                loadAllData();

                ShowErrorDialog.showErrorDialog(
                        "Delete Ad Success",
                        "The ad deleted successfully",
                        null,
                        "INFORMATION"
                );
            } catch (Exception e) {
                ShowErrorDialog.showErrorDialog(
                        "Delete Ad Error",
                        "Failed to delete ad",
                        "There was a problem deleting ad. Please try again later.",
                        "ERROR"
                );
            }
        }
    }

    @FXML
    private void restoreAd() {
        Advertisement selected = deletedAdsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ShowErrorDialog.showErrorDialog(
                    "Selection Error",
                    "No ad selected",
                    "Please select an ad",
                    "ERROR"
            );
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Restore");
        confirm.setHeaderText("Restore advertisement?");
        confirm.setContentText("Are you sure you want to restore '" + selected.getTitle() + "'?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                Map<String, Object> result = AdminService.restoreAd(selected.getId());
                loadAllData();
                ShowErrorDialog.showErrorDialog(
                        "Restore Ad Success",
                        (String) result.get("message"),
                        null,
                        "INFORMATION"
                );
            } catch (Exception e) {
                ShowErrorDialog.showErrorDialog(
                        "Restore Ad Error",
                        "Failed to restore ad",
                        "There was a problem restoring ad. Please try again later.",
                        "ERROR"
                );
            }
        }
    }

    @FXML
    private void blockUser() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ShowErrorDialog.showErrorDialog(
                    "Selection Error",
                    "No ad selected",
                    "Please select a user",
                    "ERROR"
            );
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Block");
        confirm.setHeaderText("Block user?");
        confirm.setContentText("Are you sure you want to block '" + selected.getUsername() + "'?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                Map<String, Object> result = AdminService.blockUser(selected.getId());
                loadUsers();
                ShowErrorDialog.showErrorDialog(
                        "Block User Success",
                        (String) result.get("message"),
                        null,
                        "INFORMATION"
                );
            } catch (Exception e) {
                ShowErrorDialog.showErrorDialog(
                        "Block User Error",
                        "Failed to block user",
                        "There was a problem blocking user. Please try again later.",
                        "ERROR"
                );
            }
        }
    }

    @FXML
    private void unblockUser() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ShowErrorDialog.showErrorDialog(
                    "Selection Error",
                    "No ad selected",
                    "Please select a user",
                    "ERROR"
            );
            return;
        }

        try {
            Map<String, Object> result = AdminService.unblockUser(selected.getId());
            loadUsers();
            ShowErrorDialog.showErrorDialog(
                    "Unblock User Success",
                    (String) result.get("message"),
                    null,
                    "INFORMATION"
            );
        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Unblock User Error",
                    "Failed to unblock user",
                    "There was a problem unblocking user. Please try again later.",
                    "ERROR"
            );
        }
    }


    @FXML
    private void handleAddCity() {
        String name = cityNameField.getText().trim();
        String province = cityProvinceField.getText().trim();

        if (name.isEmpty()) {
            ShowErrorDialog.showErrorDialog(
                    "Add City Error",
                    "Failed to add city",
                    "City name is required. Please enter name for city.",
                    "ERROR"
            );
            return;
        }

        try {
            City city = new City();
            city.setName(name);
            city.setProvince(province.isEmpty() ? null : province);

            CityService.createCity(city);
            cityNameField.clear();
            cityProvinceField.clear();
            loadCities();
            ShowErrorDialog.showErrorDialog(
                    "Add City Success",
                    "City added successfully",
                    null,
                    "INFORMATION"
            );
        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Add City Error",
                    "Failed to add city",
                    "There was a problem adding city. Please try again later.",
                    "ERROR"
            );
        }
    }

    @FXML
    private void handleEditCity() {
        City selected = cityListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ShowErrorDialog.showErrorDialog(
                    "Selection Error",
                    "No ad selected",
                    "Please select a city",
                    "ERROR"
            );
            return;
        }

        String newName = cityNameField.getText().trim();
        String newProvince = cityProvinceField.getText().trim();

        if (newName.isEmpty()) {
            ShowErrorDialog.showErrorDialog(
                    "Update City Error",
                    "Failed to update city",
                    "City name is required. Please enter name for city.",
                    "ERROR"
            );
            return;
        }

        try {
            selected.setName(newName);
            selected.setProvince(newProvince.isEmpty() ? null : newProvince);
            CityService.updateCity(selected.getId(), selected);
            loadCities();
            ShowErrorDialog.showErrorDialog(
                    "Update City Success",
                    "City updated successfully",
                    null,
                    "INFORMATION"
            );
        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Update City Error",
                    "Failed to update city",
                    "There was a problem updating city. Please try again later.",
                    "ERROR"
            );
        }
    }

    @FXML
    private void handleDeleteCity() {
        City selected = cityListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ShowErrorDialog.showErrorDialog(
                    "Selection Error",
                    "No ad selected",
                    "Please select a city",
                    "ERROR"
            );
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete city?");
        confirm.setContentText("Are you sure you want to delete '" + selected.getName() + "'?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                String result = CityService.deleteCity(selected.getId());
                loadCities();
                ShowErrorDialog.showErrorDialog(
                        "Delete City Success",
                        result,
                        null,
                        "INFORMATION"
                );
            } catch (Exception e) {
                ShowErrorDialog.showErrorDialog(
                        "Delete City Error",
                        "Failed to delete city",
                        "There was a problem deleting city. Please try again later.",
                        "ERROR"
                );
            }
        }
    }

    @FXML
    private void handleAddCategory() {
        String name = categoryNameField.getText().trim();
        String description = categoryDescriptionField.getText().trim();
        Category parent = categoryParentCombo.getValue();

        if (name.isEmpty()) {
            ShowErrorDialog.showErrorDialog(
                    "Add category Error",
                    "Failed to add category",
                    "Category name is required. Please enter name for category.",
                    "ERROR"
            );
            return;
        }

        try {
            Category category = new Category();
            category.setName(name);
            category.setDescription(description.isEmpty() ? null : description);
            if (parent != null) {
                category.setParentId(parent.getId());
            }

            CategoryService.createCategory(category);
            categoryNameField.clear();
            categoryDescriptionField.clear();
            categoryParentCombo.setValue(null);
            loadCategories(); // This will refresh both list and parent combo
            ShowErrorDialog.showErrorDialog(
                    "Add Category Success",
                    "Category added successfully",
                    null,
                    "INFORMATION"
            );
        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Add Category Error",
                    "Failed to add category",
                    "There was a problem adding category. Please try again later.",
                    "ERROR"
            );
        }
    }

    @FXML
    private void handleEditCategory() {
        Category selected = categoryListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ShowErrorDialog.showErrorDialog(
                    "Selection Error",
                    "No ad selected",
                    "Please select a category",
                    "ERROR"
            );
            return;
        }

        String newName = categoryNameField.getText().trim();
        String newDescription = categoryDescriptionField.getText().trim();
        Category newParent = categoryParentCombo.getValue();

        if (newName.isEmpty()) {
            ShowErrorDialog.showErrorDialog(
                    "Update Category Error",
                    "Failed to update category",
                    "Category name is required. Please enter name for category.",
                    "ERROR"
            );
            return;
        }

        try {
            selected.setName(newName);
            selected.setDescription(newDescription.isEmpty() ? null : newDescription);
            if (newParent != null) {
                selected.setParentId(newParent.getId());
            } else {
                selected.setParentId(null);
            }

            CategoryService.updateCategory(selected.getId(), selected);
            loadCategories();
            ShowErrorDialog.showErrorDialog(
                    "Update Category Success",
                    "Category updated successfully",
                    null,
                    "INFORMATION"
            );
        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Update Category Error",
                    "Failed to update category",
                    "There was a problem updating category. Please try again later.",
                    "ERROR"
            );
        }
    }

    @FXML
    private void handleDeleteCategory() {
        Category selected = categoryListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ShowErrorDialog.showErrorDialog(
                    "Selection Error",
                    "No ad selected",
                    "Please select a category",
                    "ERROR"
            );
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete category?");
        confirm.setContentText("Are you sure you want to delete '" + selected.getName() + "'?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                String result = CategoryService.deleteCategory(selected.getId());
                loadCategories();
                ShowErrorDialog.showErrorDialog(
                        "Delete Category Success",
                        result,
                        null,
                        "INFORMATION"
                );
            } catch (Exception e) {
                ShowErrorDialog.showErrorDialog(
                        "Delete Category Error",
                        "Failed to delete category",
                        "There was a problem deleting category. Please try again later.",
                        "ERROR"
                );
            }
        }
    }

    // In AdminDashboard.java

    /**
     * Loads all users (default view).
     */
    @FXML
    private void showAllUsers() {
        loadUsers(); // reuses existing method that loads all users
    }

    /**
     * Loads only blocked users.
     */
    @FXML
    private void showBlockedUsers() {
        try {
            List<User> blockedUsers = AdminService.getBlockedUsers();
            users.setAll(blockedUsers);
            usersTable.setItems(users);
            usersCountLabel.setText("Blocked Users: " + users.size());
        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Load Users Error",
                    "Failed to load blocked users",
                    e.getMessage(),
                    "ERROR"
            );
        }
    }

    /**
     * Loads only active users.
     */
    @FXML
    private void showActiveUsers() {
        try {
            List<User> activeUsers = AdminService.getActiveUsers();
            users.setAll(activeUsers);
            usersTable.setItems(users);
            usersCountLabel.setText("Active Users: " + users.size());
        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Load Users Error",
                    "Failed to load active users",
                    e.getMessage(),
                    "ERROR"
            );
        }
    }

    // REFRESH

    @FXML
    private void refreshAll() {
        loadAllData();
    }

    // NAVIGATION

    @FXML
    private void goToHome() {
        // Admin stays in admin panel
    }

    // HELPERS

    private ListView<Advertisement> getCurrentListView() {
        TabPane tabPane = (TabPane) pendingAdsListView.getParent().getParent().getParent();
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        String tabText = selectedTab.getText();

        switch (tabText) {
            case "Pending":
                return pendingAdsListView;
            case "Active":
                return activeAdsListView;
            case "Rejected":
                return rejectedAdsListView;
            case "Sold":
                return soldAdsListView;
            case "Deleted":
                return deletedAdsListView;
            default:
                return pendingAdsListView;
        }
    }
}
package com.controller;

import com.model.Advertisement;
import com.model.Category;
import com.model.City;
import com.model.User;
import com.service.AdminService;
import com.service.AdminService;
import com.service.CategoryService;
import com.service.CityService;
import com.util.NavigationUtil;
import com.view.ShowErrorDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.Map;

public class AdminDashboard {

    // FXML FIELDS - PENDING ADS

    @FXML
    private ListView<Advertisement> pendingAdsListView;

    @FXML
    private Label pendingCountLabel;

    @FXML
    private Label pendingErrorLabel;

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
    private Label activeErrorLabel;

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
    private Label rejectedErrorLabel;

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
    private Label soldErrorLabel;

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
    private Label deletedErrorLabel;

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
    private Label usersErrorLabel;

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
    private Label cityErrorLabel;


    // FXML FIELDS - CATEGORIES

    @FXML
    private ListView<Category> categoryListView;

    @FXML
    private TextField categoryNameField;

    @FXML
    private TextField categoryDescriptionField;

    @FXML
    private ComboBox<Category> categoryParentCombo;

    @FXML
    private Label categoryErrorLabel;




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
        setupAdListView(pendingAdsListView);
        setupAdListView(activeAdsListView);
        setupAdListView(rejectedAdsListView);
        setupAdListView(soldAdsListView);
        setupAdListView(deletedAdsListView);

        setupUserTable();

        // Setup sort combo boxes
        setupSortComboBoxes();

        loadAllData();
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
        listView.setCellFactory(lv -> new ListCell<Advertisement>() {
            @Override
            protected void updateItem(Advertisement ad, boolean empty) {
                super.updateItem(ad, empty);
                if (empty || ad == null) {
                    setText(null);
                    setStyle("");
                } else {
                    StringBuilder display = new StringBuilder();
                    display.append(ad.getTitle()).append("\n");
                    display.append("Price: ").append(ad.getPrice()).append(" T");
                    if (ad.getDescription() != null && !ad.getDescription().isEmpty()) {
                        String desc = ad.getDescription();
                        if (desc.length() > 60) {
                            desc = desc.substring(0, 60) + "...";
                        }
                        display.append("\n").append(desc);
                    }
                    if (ad.getOwner() != null) {
                        display.append("\nOwner: ").append(ad.getOwner().getUsername());
                    }
                    setText(display.toString());
                    setStyle("-fx-padding: 8 12; -fx-border-color: #e9ecef; -fx-border-width: 0 0 1 0;");
                }
            }
        });
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
            List<Advertisement> ads = AdminService.getAdsByStatus("ACTIVE", sortBy, sortOrder);
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
            List<City> cityList = CityService.getAllCities();
            cities.setAll(cityList);
            cityListView.setItems(cities);
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
            categoryListView.setItems(categories);

            // Update parent combo box
            updateCategoryParentCombo();

        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Load Categories Error",
                    "Failed to load categories",
                    "There was a problem loading categories. Please try again later.",
                    "ERROR"
            );
        }
    }

    private void loadDashboardStats() {
        try {
            Map<String, Object> stats = AdminService.getDashboardStats();

            totalAdsLabel.setText("Total: " + stats.get("totalAds"));
            pendingCountLabel.setText("Pending: " + stats.get("pendingAds"));
            activeCountLabel.setText("Active: " + stats.get("activeAds"));
            // You can add more stats here

        } catch (Exception e) {
            // Optional: log error
        }
    }

    private void updateCategoryParentCombo() {
        categoryParentCombo.getItems().clear();
        categoryParentCombo.getItems().addAll(categories);
        categoryParentCombo.setPromptText("Parent (optional)");
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
            Map<String,Object> result = AdminService.approveAd(selected);
            loadPendingAds();
            loadActiveAds();

            ShowErrorDialog.showErrorDialog(
                    "Approve Ad Success",
                    (String)result.get("message"),
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
            pendingErrorLabel.setText("Please select an ad to reject");
            return;
        }

        try {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Reject Ad");
            dialog.setHeaderText("Enter rejection reason:");
            dialog.setContentText("Reason:");

            String reason = dialog.showAndWait().orElse("No reason provided");
            selected.setRejectionReason(reason);
            Map<String,Object> result = AdminService.rejectAd(selected);
            loadPendingAds();
            loadRejectedAds();

            ShowErrorDialog.showErrorDialog(
                    "Reject Ad Success",
                    (String)result.get("message"),
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
            showError("Please select an ad to delete");
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
            deletedErrorLabel.setText("Please select an ad to restore");
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
            usersErrorLabel.setText("Please select a user to block");
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
            usersErrorLabel.setText("Please select a user to unblock");
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
            cityErrorLabel.setText("City name is required");
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
            cityErrorLabel.setText("Please select a city to edit");
            return;
        }

        String newName = cityNameField.getText().trim();
        String newProvince = cityProvinceField.getText().trim();

        if (newName.isEmpty()) {
            cityErrorLabel.setText("City name is required");
            return;
        }

        try {
            selected.setName(newName);
            selected.setProvince(newProvince.isEmpty() ? null : newProvince);
            CityService.updateCity(selected.getId(), selected);
            loadCities();
            cityErrorLabel.setText("City updated successfully");
        } catch (Exception e) {
            cityErrorLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteCity() {
        City selected = cityListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            cityErrorLabel.setText("Please select a city to delete");
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
                cityErrorLabel.setText(result);
            } catch (Exception e) {
                cityErrorLabel.setText("Error: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleAddCategory() {
        String name = categoryNameField.getText().trim();
        String description = categoryDescriptionField.getText().trim();
        Category parent = categoryParentCombo.getValue();

        if (name.isEmpty()) {
            categoryErrorLabel.setText("Category name is required");
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
            loadCategories();
            categoryErrorLabel.setText("Category added successfully");
        } catch (Exception e) {
            categoryErrorLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditCategory() {
        Category selected = categoryListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            categoryErrorLabel.setText("Please select a category to edit");
            return;
        }

        String newName = categoryNameField.getText().trim();
        String newDescription = categoryDescriptionField.getText().trim();
        Category newParent = categoryParentCombo.getValue();

        if (newName.isEmpty()) {
            categoryErrorLabel.setText("Category name is required");
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
            categoryErrorLabel.setText("Category updated successfully");
        } catch (Exception e) {
            categoryErrorLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteCategory() {
        Category selected = categoryListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            categoryErrorLabel.setText("Please select a category to delete");
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
                categoryErrorLabel.setText(result);
            } catch (Exception e) {
                categoryErrorLabel.setText("Error: " + e.getMessage());
            }
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
            case "Pending": return pendingAdsListView;
            case "Active": return activeAdsListView;
            case "Rejected": return rejectedAdsListView;
            case "Sold": return soldAdsListView;
            case "Deleted": return deletedAdsListView;
            default: return pendingAdsListView;
        }
    }

    private void showError(String message) {
        TabPane tabPane = (TabPane) pendingAdsListView.getParent().getParent().getParent();
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        String tabText = selectedTab.getText();

        switch (tabText) {
            case "Pending": pendingErrorLabel.setText(message); break;
            case "Active": activeErrorLabel.setText(message); break;
            case "Rejected": rejectedErrorLabel.setText(message); break;
            case "Sold": soldErrorLabel.setText(message); break;
            case "Deleted": deletedErrorLabel.setText(message); break;
            default: break;
        }
    }

    private void showSuccess(String message) {
        showError(message);
    }
}
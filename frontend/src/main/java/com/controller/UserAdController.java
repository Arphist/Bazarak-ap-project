package com.controller;

import com.BazarakFrontendApplication;
import com.model.Advertisement;
import com.service.AdService;
import com.service.UserAdvertisementService;
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
import java.util.Map;

public class UserAdController {

    // ============================================
    // FXML FIELDS
    // ============================================

    @FXML
    private ListView<Advertisement> myAdsListView;

    @FXML
    private Label countLabel;

    @FXML
    private ComboBox<String> statusFilterCombo;

    @FXML
    private ComboBox<String> sortByCombo;

    @FXML
    private Button refreshButton;

    @FXML
    private VBox dashboardContainer;

    @FXML
    private Label totalLabel;

    @FXML
    private Label pendingLabel;

    @FXML
    private Label activeLabel;

    @FXML
    private Label rejectedLabel;

    @FXML
    private Label soldLabel;

    @FXML
    private Label deletedLabel;

    // ============================================
    // NEW: BUTTONS FOR UPDATE & DELETE
    // ============================================

    @FXML
    private Button updateAdButton;

    @FXML
    private Button deleteAdButton;

    @FXML
    private Button markAsSoldButton;

    @FXML
    private Label errorLabel;

    // ============================================
    // DATA
    // ============================================

    private ObservableList<Advertisement> myAds = FXCollections.observableArrayList();
    private String currentStatusFilter = "ALL";

    // ============================================
    // INITIALIZE
    // ============================================

    @FXML
    private void initialize() {
        // Check if user is logged in
        if (!SessionManager.isLoggedIn()) {
            NavigationUtil.goToLogin();
            return;
        }

        // Setup list view
        myAdsListView.setItems(myAds);
        myAdsListView.setCellFactory(lv -> new AdCell());
        myAdsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Advertisement selected = myAdsListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    DataHolder.setSelectedAdId(selected.getId());
                    BazarakFrontendApplication.showAdDetailsPage();
                }
            }
        });

        if (myAds.isEmpty()) {
            myAdsListView.setPlaceholder(new Label("No ads found"));
        } else {
            myAdsListView.setPlaceholder(null);
        }

        // =====  Enable/Disable/markAsSold buttons based on selection =====
        myAdsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isSelected = newVal != null;
            updateAdButton.setDisable(!isSelected);
            markAsSoldButton.setDisable(!isSelected);
            deleteAdButton.setDisable(!isSelected);
        });

        // Setup filter combo box
        statusFilterCombo.setItems(FXCollections.observableArrayList(
                "ALL",
                "PENDING",
                "ACCEPTED",
                "REJECTED",
                "SOLD",
                "DELETED"
        ));
        statusFilterCombo.setValue("ALL");

        // Setup sort combo box
        sortByCombo.setItems(FXCollections.observableArrayList(
                "Newest First",
                "Oldest First",
                "Price: Low to High",
                "Price: High to Low"
        ));
        sortByCombo.setValue("Newest First");

        // Initially disable buttons
        updateAdButton.setDisable(true);
        deleteAdButton.setDisable(true);
        markAsSoldButton.setDisable(true);


        // Load data
        loadDashboardStats();
        loadMyAds();
    }

    // ============================================
    // LOAD MY ADS
    // ============================================

    @FXML
    private void loadMyAds() {
        String sortBy = parseSortBy(sortByCombo.getValue());
        String sortOrder = parseSortOrder(sortByCombo.getValue());

        try {
            List<Advertisement> ads;

            if ("ALL".equals(currentStatusFilter)) {
                ads = UserAdvertisementService.getMyAds(sortBy, sortOrder);
            } else {
                ads = UserAdvertisementService.getMyAdsByStatus(currentStatusFilter, sortBy, sortOrder);
            }

            myAds.clear();
            myAds.addAll(ads);
            myAdsListView.setItems(myAds);

            countLabel.setText("Total: " + myAds.size());

        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Advertisement Error",
                    "Failed to load my ads",
                    e.getMessage(),
                    "ERROR"
            );
        }
    }

    // ============================================
    // DASHBOARD STATS
    // ============================================

    @FXML
    private void loadDashboardStats() {
        try {
            Map<String, Map<String, Object>> dashboard = UserAdvertisementService.getMyAdsDashboard();

            totalLabel.setText("Total: " + getCount(dashboard, "total"));
            pendingLabel.setText("Pending: " + getCount(dashboard, "pending"));
            activeLabel.setText("Active: " + getCount(dashboard, "active"));
            rejectedLabel.setText("Rejected: " + getCount(dashboard, "rejected"));
            soldLabel.setText("Sold: " + getCount(dashboard, "sold"));
            deletedLabel.setText("Deleted: " + getCount(dashboard, "deleted"));

        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Dashboard Error",
                    "Failed to load dashboard statistics",
                    e.getMessage(),
                    "ERROR"
            );
        }
    }

    private long getCount(Map<String, Map<String, Object>> dashboard, String key) {
        Map<String, Object> group = dashboard.get(key);
        if (group != null) {
            Object count = group.get("count");
            if (count instanceof Number) {
                return ((Number) count).longValue();
            }
        }
        return 0;
    }

    // ============================================
    // FILTER / SORT
    // ============================================

    @FXML
    private void applyFilter() {
        currentStatusFilter = statusFilterCombo.getValue();
        loadMyAds();
    }

    @FXML
    private void applySort() {
        loadMyAds();
    }

    // ============================================
    // REFRESH
    // ============================================

    @FXML
    private void refreshAll() {
        loadDashboardStats();
        loadMyAds();
    }

    // ============================================
    // UPDATE AD (NEW)
    // ============================================

    @FXML
    private void handleUpdateAd() {
        Advertisement selected = myAdsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ShowErrorDialog.showErrorDialog(
                    "Update Error",
                    "No ad selected",
                    "Please select an ad to update.",
                    "WARNING"
            );
            return;
        }

        // TODO: Navigate to Update Ad page or open a dialog
        DataHolder.setSelectedAdId(selected.getId());
        // NavigationUtil.goToUpdateAd();
        ShowErrorDialog.showErrorDialog(
                "Update Feature",
                "Coming Soon",
                "Update feature is not yet implemented.",
                "INFORMATION"
        );
    }

    // ============================================
    // DELETE AD (NEW)
    // ============================================

    @FXML
    private void handleDeleteAd() {
        Advertisement selected = myAdsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ShowErrorDialog.showErrorDialog(
                    "Delete Error",
                    "No ad selected",
                    "Please select an ad to delete.",
                    "WARNING"
            );
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Ad");
        confirm.setHeaderText("Delete advertisement?");
        confirm.setContentText("Are you sure you want to delete '" + selected.getTitle() + "'?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                AdService.deleteAd(selected);
                myAds.remove(selected);
                loadDashboardStats();
                ShowErrorDialog.showErrorDialog(
                        "Success",
                        "Ad Deleted",
                        "Advertisement deleted successfully.",
                        "INFORMATION"
                );
            } catch (Exception e) {
                ShowErrorDialog.showErrorDialog(
                        "Delete Error",
                        "Failed to delete ad",
                        e.getMessage(),
                        "ERROR"
                );
            }
        }
    }

    @FXML
    private void handleMarkAsSold() {
        Advertisement selected = myAdsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ShowErrorDialog.showErrorDialog(
                    "Mark as Sold Error",
                    "No ad selected",
                    "Please select an ad to mark as sold.",
                    "WARNING"
            );
            return;
        }


        if ("SOLD".equals(selected.getStatus())) {
            ShowErrorDialog.showErrorDialog(
                    "Mark as Sold Error",
                    "Already Sold",
                    "This ad is already marked as sold.",
                    "WARNING"
            );
            return;
        }


        if (!"ACCEPTED".equals(selected.getStatus())) {
            ShowErrorDialog.showErrorDialog(
                    "Mark as Sold Error",
                    "Invalid Status",
                    "Only active ads (ACCEPTED) can be marked as sold.",
                    "WARNING"
            );
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Mark as Sold");
        confirm.setHeaderText("Mark advertisement as sold?");
        confirm.setContentText("Are you sure you want to mark '" + selected.getTitle() + "' as sold?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {

                AdService.markAsSold(selected);


                selected.setStatus("SOLD");
                myAdsListView.refresh();
                loadDashboardStats();

                ShowErrorDialog.showErrorDialog(
                        "Success",
                        "Ad Marked as Sold",
                        "Advertisement marked as sold successfully.",
                        "INFORMATION"
                );
            } catch (Exception e) {
                ShowErrorDialog.showErrorDialog(
                        "Mark as Sold Error",
                        "Failed to mark ad as sold",
                        e.getMessage(),
                        "ERROR"
                );
            }
        }
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    private String parseSortBy(String value) {
        if (value == null) return "created_at";
        switch (value) {
            case "Oldest First":
            case "Newest First":
                return "created_at";
            case "Price: Low to High":
            case "Price: High to Low":
                return "price";
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
            default:
                return "desc";
        }
    }

    // ============================================
    // NAVIGATION
    // ============================================

    @FXML
    private void goBack() {
        NavigationUtil.goBack();
    }

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
}
package com.controller;

import com.BazarakFrontendApplication;
import com.model.Advertisement;
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
    private void goToHome() {
        NavigationUtil.goToHome();
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
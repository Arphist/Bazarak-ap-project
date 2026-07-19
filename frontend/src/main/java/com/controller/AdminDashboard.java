package com.controller;

import com.model.Advertisement;
import com.model.User;
import com.service.AdminService;
import com.util.NavigationUtil;
import com.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.Map;

public class AdminDashboard {

    // ============================================
    // FXML FIELDS - TAB 1: PENDING ADS
    // ============================================

    @FXML
    private TableView<Advertisement> pendingAdsTable;

    @FXML
    private TableColumn<Advertisement, Long> pendingIdCol;

    @FXML
    private TableColumn<Advertisement, String> pendingTitleCol;

    @FXML
    private TableColumn<Advertisement, String> pendingOwnerCol;

    @FXML
    private TableColumn<Advertisement, Long> pendingPriceCol;

    @FXML
    private TableColumn<Advertisement, String> pendingStatusCol;

    @FXML
    private Label pendingCountLabel;

    @FXML
    private Label pendingErrorLabel;

    // ============================================
    // FXML FIELDS - TAB 2: ALL ADS
    // ============================================

    @FXML
    private TableView<Advertisement> allAdsTable;

    @FXML
    private TableColumn<Advertisement, Long> allIdCol;

    @FXML
    private TableColumn<Advertisement, String> allTitleCol;

    @FXML
    private TableColumn<Advertisement, String> allOwnerCol;

    @FXML
    private TableColumn<Advertisement, Long> allPriceCol;

    @FXML
    private TableColumn<Advertisement, String> allStatusCol;

    @FXML
    private Label allAdsCountLabel;

    @FXML
    private Label allAdsErrorLabel;

    // ============================================
    // FXML FIELDS - TAB 3: USERS
    // ============================================

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

    // ============================================
    // DATA
    // ============================================

    private ObservableList<Advertisement> pendingAds = FXCollections.observableArrayList();
    private ObservableList<Advertisement> allAds = FXCollections.observableArrayList();
    private ObservableList<User> users = FXCollections.observableArrayList();

    // ============================================
    // INITIALIZE
    // ============================================

    @FXML
    private void initialize() {
        // Check if user is admin
        if (!SessionManager.isAdmin()) {
            NavigationUtil.goToHome();
            return;
        }

        // Setup pending ads table
        pendingIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        pendingTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        pendingOwnerCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getOwner() != null ?
                                cellData.getValue().getOwner().getFullName() : "Unknown"
                )
        );
        pendingPriceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        pendingStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Setup all ads table
        allIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        allTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        allOwnerCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getOwner() != null ?
                                cellData.getValue().getOwner().getFullName() : "Unknown"
                )
        );
        allPriceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        allStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Setup users table
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        userUsernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        userFullNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        userEmailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        userStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        userRoleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Load data
        loadPendingAds();
        loadAllAds();
        loadUsers();
    }

    // ============================================
    // LOAD PENDING ADS
    // ============================================

    private void loadPendingAds() {
        try {
            List<Advertisement> ads = AdminService.getPendingAds("created_at", "asc");
            pendingAds.clear();
            pendingAds.addAll(ads);
            pendingAdsTable.setItems(pendingAds);
            pendingCountLabel.setText("Pending: " + pendingAds.size());
            pendingErrorLabel.setText("");
        } catch (Exception e) {
            pendingErrorLabel.setText("Failed to load pending ads: " + e.getMessage());
        }
    }

    // ============================================
    // APPROVE AD
    // ============================================

    @FXML
    private void approveAd() {
        Advertisement selected = pendingAdsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            pendingErrorLabel.setText("Please select an ad to approve");
            return;
        }

        try {
            Map<String, Object> result = AdminService.approveAd(selected);
            pendingErrorLabel.setText("✅ " + result.getOrDefault("message", "Ad approved"));
            loadPendingAds();
            loadAllAds();
        } catch (Exception e) {
            pendingErrorLabel.setText("Failed to approve ad: " + e.getMessage());
        }
    }

    // ============================================
    // REJECT AD
    // ============================================

    @FXML
    private void rejectAd() {
        Advertisement selected = pendingAdsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            pendingErrorLabel.setText("Please select an ad to reject");
            return;
        }

        try {
            Map<String, Object> result = AdminService.rejectAd(selected);
            pendingErrorLabel.setText("✅ " + result.getOrDefault("message", "Ad rejected"));
            loadPendingAds();
            loadAllAds();
        } catch (Exception e) {
            pendingErrorLabel.setText("Failed to reject ad: " + e.getMessage());
        }
    }

    // ============================================
    // DELETE AD (Admin)
    // ============================================

    @FXML
    private void deleteAd() {
        Advertisement selected = allAdsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            allAdsErrorLabel.setText("Please select an ad to delete");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Ad");
        alert.setHeaderText("Delete advertisement: " + selected.getTitle());
        alert.setContentText("Are you sure you want to permanently delete this ad?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    AdminService.deleteAd(selected);
                    allAdsErrorLabel.setText("✅ Ad deleted successfully");
                    loadAllAds();
                    loadPendingAds();
                } catch (Exception e) {
                    allAdsErrorLabel.setText("Failed to delete ad: " + e.getMessage());
                }
            }
        });
    }

    // ============================================
    // LOAD ALL ADS
    // ============================================

    private void loadAllAds() {
        try {
            List<Advertisement> ads = AdminService.getAllAds("created_at", "desc");
            allAds.clear();
            allAds.addAll(ads);
            allAdsTable.setItems(allAds);
            allAdsCountLabel.setText("Total Ads: " + allAds.size());
            allAdsErrorLabel.setText("");
        } catch (Exception e) {
            allAdsErrorLabel.setText("Failed to load all ads: " + e.getMessage());
        }
    }

    // ============================================
    // LOAD USERS
    // ============================================

    private void loadUsers() {
        try {
            List<User> userList = AdminService.getAllUsers();
            users.clear();
            users.addAll(userList);
            usersTable.setItems(users);
            usersCountLabel.setText("Total Users: " + users.size());
            usersErrorLabel.setText("");
        } catch (Exception e) {
            usersErrorLabel.setText("Failed to load users: " + e.getMessage());
        }
    }

    // ============================================
    // BLOCK / UNBLOCK USER
    // ============================================

    @FXML
    private void blockUser() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            usersErrorLabel.setText("Please select a user");
            return;
        }

        if ("BANNED".equals(selected.getStatus())) {
            usersErrorLabel.setText("User is already blocked");
            return;
        }

        try {
            Map<String, Object> result = AdminService.blockUser(selected.getId());
            usersErrorLabel.setText("✅ " + result.getOrDefault("message", "User blocked"));
            loadUsers();
        } catch (Exception e) {
            usersErrorLabel.setText("Failed to block user: " + e.getMessage());
        }
    }

    @FXML
    private void unblockUser() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            usersErrorLabel.setText("Please select a user");
            return;
        }

        if (!"BANNED".equals(selected.getStatus())) {
            usersErrorLabel.setText("User is not blocked");
            return;
        }

        try {
            Map<String, Object> result = AdminService.unblockUser(selected.getId());
            usersErrorLabel.setText("✅ " + result.getOrDefault("message", "User unblocked"));
            loadUsers();
        } catch (Exception e) {
            usersErrorLabel.setText("Failed to unblock user: " + e.getMessage());
        }
    }
    // ============================================
    // REFRESH ALL
    // ============================================

    @FXML
    private void refreshAll() {
        loadPendingAds();
        loadAllAds();
        loadUsers();
    }

    // ============================================
    // NAVIGATION
    // ============================================

    @FXML
    private void goToHome() {
        NavigationUtil.goToHome();
    }






}
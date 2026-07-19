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

}
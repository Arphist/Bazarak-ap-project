package com.controller;

import com.model.Advertisement;
import com.model.User;
import com.service.AdminService;
import com.util.NavigationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.HashMap;
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


    // FXML FIELDS - ACTIVE ADS


    @FXML
    private ListView<Advertisement> activeAdsListView;

    @FXML
    private Label activeCountLabel;

    @FXML
    private Label activeErrorLabel;


    // FXML FIELDS - REJECTED ADS


    @FXML
    private ListView<Advertisement> rejectedAdsListView;

    @FXML
    private Label rejectedCountLabel;

    @FXML
    private Label rejectedErrorLabel;


    // FXML FIELDS - SOLD ADS


    @FXML
    private ListView<Advertisement> soldAdsListView;

    @FXML
    private Label soldCountLabel;

    @FXML
    private Label soldErrorLabel;


    // FXML FIELDS - DELETED ADS


    @FXML
    private ListView<Advertisement> deletedAdsListView;

    @FXML
    private Label deletedCountLabel;

    @FXML
    private Label deletedErrorLabel;


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


    // DATA


    private ObservableList<Advertisement> pendingAds = FXCollections.observableArrayList();
    private ObservableList<Advertisement> activeAds = FXCollections.observableArrayList();
    private ObservableList<Advertisement> rejectedAds = FXCollections.observableArrayList();
    private ObservableList<Advertisement> soldAds = FXCollections.observableArrayList();
    private ObservableList<Advertisement> deletedAds = FXCollections.observableArrayList();
    private ObservableList<User> users = FXCollections.observableArrayList();


    // INITIALIZE


    @FXML
    private void initialize() {
        setupAdListView(pendingAdsListView);
        setupAdListView(activeAdsListView);
        setupAdListView(rejectedAdsListView);
        setupAdListView(soldAdsListView);
        setupAdListView(deletedAdsListView);

        setupUserTable();

        loadAllData();
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


    // LOAD DATA


    private void loadAllData() {
        loadPendingAds();
        loadActiveAds();
        loadRejectedAds();
        loadSoldAds();
        loadDeletedAds();
        loadUsers();
    }

    private void loadPendingAds() {
        try {
            List<Advertisement> ads = AdminService.getPendingAds("created_at", "asc");
            pendingAds.setAll(ads);
            pendingAdsListView.setItems(pendingAds);
            pendingCountLabel.setText("Pending: " + pendingAds.size());
            pendingErrorLabel.setText("");
        } catch (Exception e) {
            String msg = e.getMessage();
            pendingErrorLabel.setText("Error: " + (msg != null ? msg : "Failed to load pending ads"));
        }
    }

    private void loadActiveAds() {
        try {
            List<Advertisement> ads = AdminService.getAdsByStatus("ACTIVE", "created_at", "desc");
            activeAds.setAll(ads);
            activeAdsListView.setItems(activeAds);
            activeCountLabel.setText("Active: " + activeAds.size());
            activeErrorLabel.setText("");
        } catch (Exception e) {
            String msg = e.getMessage();
            activeErrorLabel.setText("Error: " + (msg != null ? msg : "Failed to load active ads"));
        }
    }

    private void loadRejectedAds() {
        try {
            List<Advertisement> ads = AdminService.getAdsByStatus("REJECTED", "created_at", "desc");
            rejectedAds.setAll(ads);
            rejectedAdsListView.setItems(rejectedAds);
            rejectedCountLabel.setText("Rejected: " + rejectedAds.size());
            rejectedErrorLabel.setText("");
        } catch (Exception e) {
            String msg = e.getMessage();
            rejectedErrorLabel.setText("Error: " + (msg != null ? msg : "Failed to load rejected ads"));
        }
    }

    private void loadSoldAds() {
        try {
            List<Advertisement> ads = AdminService.getAdsByStatus("SOLD", "created_at", "desc");
            soldAds.setAll(ads);
            soldAdsListView.setItems(soldAds);
            soldCountLabel.setText("Sold: " + soldAds.size());
            soldErrorLabel.setText("");
        } catch (Exception e) {
            String msg = e.getMessage();
            soldErrorLabel.setText("Error: " + (msg != null ? msg : "Failed to load sold ads"));
        }
    }

    private void loadDeletedAds() {
        try {
            List<Advertisement> ads = AdminService.getAdsByStatus("DELETED", "created_at", "desc");
            deletedAds.setAll(ads);
            deletedAdsListView.setItems(deletedAds);
            deletedCountLabel.setText("Deleted: " + deletedAds.size());
            deletedErrorLabel.setText("");
        } catch (Exception e) {
            String msg = e.getMessage();
            deletedErrorLabel.setText("Error: " + (msg != null ? msg : "Failed to load deleted ads"));
        }
    }

    private void loadUsers() {
        try {
            List<User> userList = AdminService.getAllUsers();
            users.setAll(userList);
            usersTable.setItems(users);
            usersCountLabel.setText("Total Users: " + users.size());
            usersErrorLabel.setText("");
        } catch (Exception e) {
            String msg = e.getMessage();
            usersErrorLabel.setText("Error: " + (msg != null ? msg : "Failed to load users"));
        }
    }


    // ACTIONS


    @FXML
    private void approveAd() {
        Advertisement selected = pendingAdsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            pendingErrorLabel.setText("Please select an ad to approve");
            return;
        }

        try {
            AdminService.approveAd(selected);
            loadPendingAds();
            loadActiveAds();
            pendingErrorLabel.setText("Ad approved successfully");
        } catch (Exception e) {
            String msg = e.getMessage();
            pendingErrorLabel.setText("Error: " + (msg != null ? msg : "Failed to approve ad"));
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
            AdminService.rejectAd(selected);
            loadPendingAds();
            loadRejectedAds();
            pendingErrorLabel.setText("Ad rejected successfully");
        } catch (Exception e) {
            String msg = e.getMessage();
            pendingErrorLabel.setText("Error: " + (msg != null ? msg : "Failed to reject ad"));
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
                showSuccess("Ad deleted successfully");
            } catch (Exception e) {
                String msg = e.getMessage();
                showError("Error: " + (msg != null ? msg : "Failed to delete ad"));
            }
        }
    }

    /**
     * Restores a deleted advertisement back to pending status.
     * This allows the ad to be reviewed and approved again.
     */
    @FXML
    private void restoreAd() {
        Advertisement selected = deletedAdsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            deletedErrorLabel.setText("Please select an ad to restore");
            return;
        }

        // Confirmation dialog
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Restore");
        confirm.setHeaderText("Restore advertisement?");
        confirm.setContentText("Are you sure you want to restore '" + selected.getTitle() + "'?\nIt will be sent back for admin review.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            Map<String, Object> result = AdminService.restoreAd(selected.getId());
            loadAllData();
            deletedErrorLabel.setText((String) result.get("message"));
        } catch (Exception e) {
            String msg = e.getMessage();
            deletedErrorLabel.setText("Error: " + (msg != null ? msg : "Failed to restore ad"));
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
                AdminService.blockUser(selected.getId());
                loadUsers();
                usersErrorLabel.setText("User blocked successfully");
            } catch (Exception e) {
                String msg = e.getMessage();
                usersErrorLabel.setText("Error: " + (msg != null ? msg : "Failed to block user"));
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
            AdminService.unblockUser(selected.getId());
            loadUsers();
            usersErrorLabel.setText("User unblocked successfully");
        } catch (Exception e) {
            String msg = e.getMessage();
            usersErrorLabel.setText("Error: " + (msg != null ? msg : "Failed to unblock user"));
        }
    }


    // REFRESH


    @FXML
    private void refreshAll() {
        loadAllData();
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

    private void showError(String message) {
        TabPane tabPane = (TabPane) pendingAdsListView.getParent().getParent().getParent();
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        String tabText = selectedTab.getText();

        switch (tabText) {
            case "Pending":
                pendingErrorLabel.setText(message);
                break;
            case "Active":
                activeErrorLabel.setText(message);
                break;
            case "Rejected":
                rejectedErrorLabel.setText(message);
                break;
            case "Sold":
                soldErrorLabel.setText(message);
                break;
            case "Deleted":
                deletedErrorLabel.setText(message);
                break;
            default:
                break;
        }
    }

    private void showSuccess(String message) {
        showError(message);
    }
}
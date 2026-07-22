package com.controller;

import com.BazarakFrontendApplication;
import com.model.Advertisement;
import com.model.User;
import com.service.AdService;
import com.service.UserAdvertisementService;
import com.service.UserService;
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

    @FXML
    private Label headerLabel;
    private User targetUser;  // if null, load current user's ads
    private String targetUsername;

    // ============================================
    // FXML FIELDS - ALL ADS
    // ============================================

    @FXML
    private ListView<Advertisement> allAdsListView;

    @FXML
    private ComboBox<String> allSortByCombo;

    @FXML
    private ComboBox<String> allSortOrderCombo;

    @FXML
    private Label allAdsErrorLabel;

    // ============================================
    // FXML FIELDS - PENDING ADS
    // ============================================

    @FXML
    private ListView<Advertisement> pendingAdsListView;

    @FXML
    private ComboBox<String> pendingSortByCombo;

    @FXML
    private ComboBox<String> pendingSortOrderCombo;

    @FXML
    private Label pendingErrorLabel;

    // ============================================
    // FXML FIELDS - ACTIVE ADS
    // ============================================

    @FXML
    private ListView<Advertisement> activeAdsListView;

    @FXML
    private ComboBox<String> activeSortByCombo;

    @FXML
    private ComboBox<String> activeSortOrderCombo;

    @FXML
    private Label activeErrorLabel;

    // ============================================
    // FXML FIELDS - REJECTED ADS
    // ============================================

    @FXML
    private ListView<Advertisement> rejectedAdsListView;

    @FXML
    private ComboBox<String> rejectedSortByCombo;

    @FXML
    private ComboBox<String> rejectedSortOrderCombo;

    @FXML
    private Label rejectedErrorLabel;

    // ============================================
    // FXML FIELDS - SOLD ADS
    // ============================================

    @FXML
    private ListView<Advertisement> soldAdsListView;

    @FXML
    private ComboBox<String> soldSortByCombo;

    @FXML
    private ComboBox<String> soldSortOrderCombo;

    @FXML
    private Label soldErrorLabel;

    // ============================================
    // FXML FIELDS - DELETED ADS
    // ============================================

    @FXML
    private ListView<Advertisement> deletedAdsListView;

    @FXML
    private ComboBox<String> deletedSortByCombo;

    @FXML
    private ComboBox<String> deletedSortOrderCombo;

    @FXML
    private Label deletedErrorLabel;

    // ============================================
    // FXML FIELDS - BUTTONS (Shared across all tabs)
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

    private ObservableList<Advertisement> allAds = FXCollections.observableArrayList();
    private ObservableList<Advertisement> pendingAds = FXCollections.observableArrayList();
    private ObservableList<Advertisement> activeAds = FXCollections.observableArrayList();
    private ObservableList<Advertisement> rejectedAds = FXCollections.observableArrayList();
    private ObservableList<Advertisement> soldAds = FXCollections.observableArrayList();
    private ObservableList<Advertisement> deletedAds = FXCollections.observableArrayList();

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

        // Setup all list views with AdCell
        setupListView(allAdsListView);
        setupListView(pendingAdsListView);
        setupListView(activeAdsListView);
        setupListView(rejectedAdsListView);
        setupListView(soldAdsListView);
        setupListView(deletedAdsListView);

        if (allAds.isEmpty()) {
            allAdsListView.setPlaceholder(new Label("No ads found"));
        } else {
            allAdsListView.setPlaceholder(null);
        }
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

        // Setup sort combo boxes
        setupSortComboBoxes();

        // Initially disable buttons
        updateAdButton.setDisable(true);
        deleteAdButton.setDisable(true);
        markAsSoldButton.setDisable(true);

        // Load all data
        loadAllData();
    }


    public void loadAdsForUser() {
        if (DataHolder.getSelectedUser() == null) {
            loadAllAds();
            return;
        }
        try {
            List<Advertisement> ads = AdService.getAdsByUser(DataHolder.getSelectedUser());
            allAds.setAll(ads);
            allAdsListView.setItems(allAds);
            allAdsErrorLabel.setText("");

            // Update header with the username
            setHeaderText("Ads of " + DataHolder.getSelectedUser().getUsername());

        } catch (Exception e) {
            allAdsErrorLabel.setText("Failed to load ads: " + e.getMessage());
        }
    }
    // ============================================
    // SETUP LIST VIEW
    // ============================================

    private void setupListView(ListView<Advertisement> listView) {
        listView.setCellFactory(lv -> new AdCell());
        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Advertisement selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    try {
                        Advertisement fullAd = UserAdvertisementService.getMyAd(selected.getId());
                        DataHolder.setSelectedAdId(fullAd.getId());
                        BazarakFrontendApplication.showAdDetailsPage();
                    } catch (Exception e) {
                        ShowErrorDialog.showErrorDialog(
                                "Ad Details Error",
                                "Failed to load ad details",
                                e.getMessage(),
                                "ERROR"
                        );
                    }
                }
            }
        });

        // Enable/Disable buttons based on selection
        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isSelected = newVal != null;
            updateAdButton.setDisable(!isSelected);
            markAsSoldButton.setDisable(!isSelected);
            deleteAdButton.setDisable(!isSelected);
        });
    }

    // ============================================
    // SETUP SORT COMBO BOXES
    // ============================================

    private void setupSortComboBoxes() {
        // All
        allSortByCombo.setItems(FXCollections.observableArrayList("created_at", "title", "price"));
        allSortByCombo.setValue("created_at");
        allSortOrderCombo.setItems(FXCollections.observableArrayList("asc", "desc"));
        allSortOrderCombo.setValue("desc");

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

    // ============================================
    // LOAD ALL DATA
    // ============================================

    private void loadAllData() {
        loadAllAds();
        loadPendingAds();
        loadActiveAds();
        loadRejectedAds();
        loadSoldAds();
        loadDeletedAds();
    }

    // ============================================
    // LOAD ALL ADS
    // ============================================

    private void loadAllAds() {
        try {
            String sortBy = allSortByCombo.getValue();
            String sortOrder = allSortOrderCombo.getValue();
            List<Advertisement> ads;
            if (DataHolder.getSelectedUser() != null) {
                ads = AdService.getAdsByUser(DataHolder.getSelectedUser());
                setHeaderText("Ads of " + DataHolder.getSelectedUser().getUsername());
            } else {
                ads = UserAdvertisementService.getMyAds(sortBy, sortOrder);
                setHeaderText("My Ads");  // Reset to "My Ads"
            }
            allAds.setAll(ads);
            allAdsListView.setItems(allAds);
            allAdsErrorLabel.setText("");
        } catch (Exception e) {
            allAdsErrorLabel.setText("Failed to load ads: " + e.getMessage());
        }
    }

    // ============================================
    // LOAD PENDING ADS
    // ============================================

    private void loadPendingAds() {
        try {
            String sortBy = pendingSortByCombo.getValue();
            String sortOrder = pendingSortOrderCombo.getValue();
            List<Advertisement> ads = UserAdvertisementService.getMyAdsByStatus("PENDING", sortBy, sortOrder);
            pendingAds.setAll(ads);
            pendingAdsListView.setItems(pendingAds);
            pendingErrorLabel.setText("");
        } catch (Exception e) {
            pendingErrorLabel.setText("Failed to load pending ads: " + e.getMessage());
        }
    }

    // ============================================
    // LOAD ACTIVE ADS
    // ============================================

    private void loadActiveAds() {
        try {
            String sortBy = activeSortByCombo.getValue();
            String sortOrder = activeSortOrderCombo.getValue();
            List<Advertisement> ads = UserAdvertisementService.getMyAdsByStatus("ACCEPTED", sortBy, sortOrder);
            activeAds.setAll(ads);
            activeAdsListView.setItems(activeAds);
            activeErrorLabel.setText("");
        } catch (Exception e) {
            activeErrorLabel.setText("Failed to load active ads: " + e.getMessage());
        }
    }

    // ============================================
    // LOAD REJECTED ADS
    // ============================================

    private void loadRejectedAds() {
        try {
            String sortBy = rejectedSortByCombo.getValue();
            String sortOrder = rejectedSortOrderCombo.getValue();
            List<Advertisement> ads = UserAdvertisementService.getMyAdsByStatus("REJECTED", sortBy, sortOrder);
            rejectedAds.setAll(ads);
            rejectedAdsListView.setItems(rejectedAds);
            rejectedErrorLabel.setText("");
        } catch (Exception e) {
            rejectedErrorLabel.setText("Failed to load rejected ads: " + e.getMessage());
        }
    }

    // ============================================
    // LOAD SOLD ADS
    // ============================================

    private void loadSoldAds() {
        try {
            String sortBy = soldSortByCombo.getValue();
            String sortOrder = soldSortOrderCombo.getValue();
            List<Advertisement> ads = UserAdvertisementService.getMyAdsByStatus("SOLD", sortBy, sortOrder);
            soldAds.setAll(ads);
            soldAdsListView.setItems(soldAds);
            soldErrorLabel.setText("");
        } catch (Exception e) {
            soldErrorLabel.setText("Failed to load sold ads: " + e.getMessage());
        }
    }

    // ============================================
    // LOAD DELETED ADS
    // ============================================

    private void loadDeletedAds() {
        try {
            String sortBy = deletedSortByCombo.getValue();
            String sortOrder = deletedSortOrderCombo.getValue();
            List<Advertisement> ads = UserAdvertisementService.getMyAdsByStatus("DELETED", sortBy, sortOrder);
            deletedAds.setAll(ads);
            deletedAdsListView.setItems(deletedAds);
            deletedErrorLabel.setText("");
        } catch (Exception e) {
            deletedErrorLabel.setText("Failed to load deleted ads: " + e.getMessage());
        }
    }

    // ============================================
    // SORT ACTIONS
    // ============================================

    @FXML
    private void applyAllSort() {
        loadAllAds();
    }

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

    // ============================================
    // REFRESH
    // ============================================

    @FXML
    private void refreshAll() {
        loadAllData();
    }

    // ============================================
    // UPDATE AD
    // ============================================

    @FXML
    private void handleUpdateAd() {
        ListView<Advertisement> currentListView = getCurrentListView();
        Advertisement selected = currentListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ShowErrorDialog.showErrorDialog(
                    "Update Error",
                    "No ad selected",
                    "Please select an ad to update.",
                    "WARNING"
            );
            return;
        }

        DataHolder.setSelectedAdId(selected.getId());
        ShowErrorDialog.showErrorDialog(
                "Update Feature",
                "Coming Soon",
                "Update feature is not yet implemented.",
                "INFORMATION"
        );
    }

    // ============================================
    // MARK AS SOLD
    // ============================================

    @FXML
    private void handleMarkAsSold() {
        ListView<Advertisement> currentListView = getCurrentListView();
        Advertisement selected = currentListView.getSelectionModel().getSelectedItem();
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
                loadAllData();
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
    // DELETE AD
    // ============================================

    @FXML
    private void handleDeleteAd() {
        ListView<Advertisement> currentListView = getCurrentListView();
        Advertisement selected = currentListView.getSelectionModel().getSelectedItem();
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
                loadAllData();
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

    // ============================================
    // HELPER METHODS
    // ============================================
    public void setTargetUser(User user) {
        this.targetUser = user;
    }

    public void setUsername(String username) {
        this.targetUsername = username;
    }
    public void setHeaderText(String text) {
        if (headerLabel != null) {
            headerLabel.setText(text);
        }
    }

    private ListView<Advertisement> getCurrentListView() {
        TabPane tabPane = (TabPane) allAdsListView.getParent().getParent().getParent().getParent();
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        String tabText = selectedTab.getText();

        switch (tabText) {
            case "All":
                return allAdsListView;
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
                return allAdsListView;
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
    private void goToProfile() {
        NavigationUtil.goToProfile();
    }
}
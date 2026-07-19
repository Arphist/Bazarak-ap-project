package com.controller;

import com.BazarakFrontendApplication;
import com.model.Advertisement;
import com.service.UserAdvertisementService;
import com.util.DataHolder;
import com.util.NavigationUtil;
import com.util.SessionManager;
import com.view.AdCell;
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
    private Label errorLabel;

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


}
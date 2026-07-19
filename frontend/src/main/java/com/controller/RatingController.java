package com.controller;

import com.model.Advertisement;
import com.model.Rating;
import com.model.User;
import com.service.RatingService;
import com.util.DataHolder;
import com.util.NavigationUtil;
import com.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;

public class RatingController {

    // ============================================
    // FXML FIELDS
    // ============================================

    // Rating display
    @FXML
    private Label sellerNameLabel;

    @FXML
    private Label averageRatingLabel;

    @FXML
    private Label totalRatingsLabel;

    @FXML
    private ListView<Rating> ratingsListView;

    // Create rating
    @FXML
    private ComboBox<Integer> scoreComboBox;

    @FXML
    private TextArea commentTextArea;

    @FXML
    private Label errorLabel;

    @FXML
    private Button submitRatingButton;

    // ============================================
    // DATA
    // ============================================

    private ObservableList<Rating> ratings = FXCollections.observableArrayList();
    private Long sellerId;
    private Long advertisementId;


}
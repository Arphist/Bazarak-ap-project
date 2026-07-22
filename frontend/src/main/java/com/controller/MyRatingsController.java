package com.controller;

import com.model.Rating;
import com.service.RatingService;
import com.util.NavigationUtil;
import com.view.ShowErrorDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.util.List;

public class MyRatingsController {

    // ============================================
    // FXML FIELDS
    // ============================================

    @FXML
    private ListView<Rating> ratingsListView;

    @FXML
    private Label countLabel;

    @FXML
    private Label errorLabel;

    // ============================================
    // DATA
    // ============================================

    private ObservableList<Rating> ratings = FXCollections.observableArrayList();


}
package com.controller;

import com.model.Advertisement;
import com.model.User;
//import com.service.AdService;
import com.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;

public class MainController {

    // ======== FXML FIELDS ========

    @FXML
    private TextField searchField;

    @FXML
    private ListView<String> adListView;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label errorLabel;

    // ======== STAGE ========

    private Stage primaryStage;

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }
}
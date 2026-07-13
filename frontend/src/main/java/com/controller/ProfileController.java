package com.controller;

import com.BazarakFrontendApplication;
import com.model.User;
import com.service.AuthService;
import com.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class ProfileController {

    // ============================================
    // FXML FIELDS
    // ============================================

    @FXML
    private Label usernameLabel;

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private Label errorLabel;

    @FXML
    private PasswordField oldPasswordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;
    // ============================================
    // INITIALIZE
    // ============================================

    @FXML
    private void initialize() {
        loadUserProfile();
    }

    private void loadUserProfile() {
        User currentUser = SessionManager.getCurrentUser();

        if (currentUser != null) {
            usernameLabel.setText("Username: " + currentUser.getUsername());
            fullNameField.setText(currentUser.getFullName());
            emailField.setText(currentUser.getEmail());
            phoneField.setText(currentUser.getPhoneNumber());
        } else {
            errorLabel.setText("No user logged in");
        }
    }


}
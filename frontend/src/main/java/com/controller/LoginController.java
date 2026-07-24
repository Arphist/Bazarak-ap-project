package com.controller;

import com.BazarakFrontendApplication;
import com.model.User;
import com.service.AuthService;
import com.util.NavigationUtil;
import com.util.SessionManager;
import com.view.ShowErrorDialog;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;

public class LoginController {

    // ======== FXML FIELDS ========
    @FXML
    private TextField passwordVisibleField;

    @FXML
    private ToggleButton passwordToggle;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;


    @FXML
    private void initialize() {
        // Sync password fields
        passwordField.textProperty().addListener((obs, old, newVal) -> {
            passwordVisibleField.setText(newVal);
        });

        passwordVisibleField.textProperty().addListener((obs, old, newVal) -> {
            passwordField.setText(newVal);
        });

        // Add tooltip to toggle button
        passwordToggle.setTooltip(new javafx.scene.control.Tooltip("Show/Hide Password"));
    }

    // ======== HANDLE LOGIN ========

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Check empty fields
        if (username.isEmpty() || password.isEmpty()) {
            ShowErrorDialog.showErrorDialog(
                    "Validation Error",
                    "Incomplete information",
                    "Please fill in all fields.",
                    "WARNING"
            );
            return;
        }

        try {
            // Call AuthService to login
            User user = AuthService.login(username, password);



            if (user.isAdmin()){
                BazarakFrontendApplication.showAdminDashboard();
            }else{
                goToHome();
            }

        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Login Error",
                    "Login failed",
                    e.getMessage(),
                    "ERROR"
            );
        }
    }

    @FXML
    private void togglePasswordVisibility() {
        togglePasswordField(passwordField, passwordVisibleField, passwordToggle);
    }

    private void togglePasswordField(PasswordField passwordField, TextField visibleField, ToggleButton toggle) {
        if (toggle.isSelected()) {
            visibleField.setText(passwordField.getText());
            visibleField.setVisible(true);
            visibleField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            toggle.setText("👁");
        } else {
            passwordField.setText(visibleField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            visibleField.setVisible(false);
            visibleField.setManaged(false);
            toggle.setText("👁");
        }
    }

    // ======== NAVIGATION ========

    @FXML
    private void goToRegister() {
        NavigationUtil.goToRegister();
    }


    private void goToHome() {
        NavigationUtil.goToHome();
    }



}
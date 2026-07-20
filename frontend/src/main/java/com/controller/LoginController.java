package com.controller;

import com.BazarakFrontendApplication;
import com.model.User;
import com.service.AuthService;
import com.util.NavigationUtil;
import com.util.SessionManager;
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
    private Label errorLabel;

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
            errorLabel.setText("Please fill in all fields");
            return;
        }

        try {
            // Call AuthService to login
            User user = AuthService.login(username, password);

            // Clear error and go to main page
            errorLabel.setText("");

            if (user.isAdmin()){
                BazarakFrontendApplication.showAdminDashboard();
            }else{
                BazarakFrontendApplication.showHomePage();
            }

        } catch (Exception e) {
            errorLabel.setText("Login failed: " + e.getMessage());
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

    //todo delete this later
    @FXML
    private void handleSkipLogin() {
        // Create a fake user for testing
        User fakeUser = new User();
        fakeUser.setId(1L);
        fakeUser.setUsername("test");
        fakeUser.setFullName("Test User");
        fakeUser.setRole("USER");
        fakeUser.setStatus("ACTIVE");

        // Save to session
        SessionManager.setCurrentUser(fakeUser);

        // Go to main page
        BazarakFrontendApplication.showHomePage();
    }
    //todo delete this later
    @FXML
    private void skipToAdmin() {
        // Create a test admin user
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("admin");
        testUser.setFullName("Admin User");
        testUser.setRole("ADMIN");
        testUser.setStatus("ACTIVE");

        // Set as current user
        SessionManager.setCurrentUser(testUser);

        // Go to Admin Dashboard
        BazarakFrontendApplication.showAdminDashboard();
    }

    // ======== NAVIGATION ========

    @FXML
    private void goToRegister() {
        NavigationUtil.goToRegister();
    }





}
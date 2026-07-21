package com.controller;

import com.model.User;
import com.service.AuthService;
import com.util.NavigationUtil;
import com.view.ShowErrorDialog;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;

public class RegisterController {

    // ======== FXML FIELDS ========
    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField passwordVisibleField;

    @FXML
    private ToggleButton passwordToggle;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField confirmPasswordVisibleField;

    @FXML
    private ToggleButton confirmPasswordToggle;

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;



    @FXML
    private void initialize() {
        // Sync password fields
        passwordField.textProperty().addListener((obs, old, newVal) -> {
            passwordVisibleField.setText(newVal);
        });

        passwordVisibleField.textProperty().addListener((obs, old, newVal) -> {
            passwordField.setText(newVal);
        });

        // Sync confirm password fields
        confirmPasswordField.textProperty().addListener((obs, old, newVal) -> {
            confirmPasswordVisibleField.setText(newVal);
        });

        confirmPasswordVisibleField.textProperty().addListener((obs, old, newVal) -> {
            confirmPasswordField.setText(newVal);
        });

        // Add tooltip to toggle buttons
        passwordToggle.setTooltip(new javafx.scene.control.Tooltip("Show/Hide Password"));
        confirmPasswordToggle.setTooltip(new javafx.scene.control.Tooltip("Show/Hide Password"));
    }

    // ======== HANDLE REGISTER ========

    @FXML
    private void handleRegister() {
        // Get input values
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        // Check empty fields
        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() ||
                phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            ShowErrorDialog.showErrorDialog(
                    "Validation Error",
                    "Incomplete information",
                    "Please fill in all fields.",
                    "WARNING"
            );
            return;
        }

        // Check password match
        if (!password.equals(confirmPassword)) {
            ShowErrorDialog.showErrorDialog(
                    "Validation Error",
                    "Password mismatch",
                    "Passwords do not match.",
                    "WARNING"
            );
            return;
        }

        // Check password length (minimum 6 characters)
        if (password.length() < 6) {
            ShowErrorDialog.showErrorDialog(
                    "Validation Error",
                    "Weak password",
                    "Password must be at least 6 characters.",
                    "WARNING"
            );
            return;
        }

        try {
            // Create user object
            User user = new User();
            user.setFullName(fullName);
            user.setUsername(username);
            user.setEmail(email);
            user.setPhoneNumber(phone);
            user.setPassword(password);

            // Call AuthService to register
            User registeredUser = AuthService.register(user);



            goToLogin();


        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Registration Error",
                    "Registration failed",
                    e.getMessage(),
                    "ERROR"
            );
        }
    }

    // TOGGLE PASSWORD VISIBILITY

    @FXML
    private void togglePasswordVisibility() {
        togglePasswordField(passwordField, passwordVisibleField, passwordToggle);
    }

    @FXML
    private void toggleConfirmPasswordVisibility() {
        togglePasswordField(confirmPasswordField, confirmPasswordVisibleField, confirmPasswordToggle);
    }

    private void togglePasswordField(PasswordField passwordField, TextField visibleField, ToggleButton toggle) {
        if (toggle.isSelected()) {
            // Show password - hide PasswordField, show TextField
            visibleField.setText(passwordField.getText());
            visibleField.setVisible(true);
            visibleField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            toggle.setText("👁");
        } else {
            // Hide password - show PasswordField, hide TextField
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
    private void goToLogin() {
        NavigationUtil.goToLogin();
    }
}
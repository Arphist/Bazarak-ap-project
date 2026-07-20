package com.controller;

import com.model.User;
import com.service.AuthService;
import com.util.NavigationUtil;
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
    private Label errorLabel;



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
            errorLabel.setText("Please fill in all fields");
            return;
        }

        // Check password match
        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Passwords do not match");
            return;
        }

        // Check password length (minimum 6 characters)
        if (password.length() < 6) {
            errorLabel.setText("Password must be at least 6 characters");
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


            // Clear error and go to login page
            errorLabel.setText("");
            goToLogin();


        } catch (Exception e) {
            errorLabel.setText("Registration failed: " + e.getMessage());
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
            toggle.setText("👁️");
        } else {
            // Hide password - show PasswordField, hide TextField
            passwordField.setText(visibleField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            visibleField.setVisible(false);
            visibleField.setManaged(false);
            toggle.setText("👁️");
        }
    }

    // ======== NAVIGATION ========

    @FXML
    private void goToLogin() {
        NavigationUtil.goToLogin();
    }
}
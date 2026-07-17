package com.controller;

import com.BazarakFrontendApplication;
import com.fasterxml.jackson.databind.util.NativeImageUtil;
import com.model.User;
import com.service.AuthService;
import com.service.UserService;
import com.util.NavigationUtil;
import com.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ProfileController {

    // FXML FIELDS

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

    @FXML
    private Label createdAtLabel;

    @FXML
    private Label updatedAtLabel;

    // INITIALIZE

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

            // Display time information
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            if (currentUser.getCreatedAt() != null) {
                createdAtLabel.setText("Account created: " + currentUser.getCreatedAt().format(formatter));
            } else {
                createdAtLabel.setText("Account created: N/A");
            }

            if (currentUser.getUpdatedAt() != null) {
                updatedAtLabel.setText("Last updated: " + currentUser.getUpdatedAt().format(formatter));
            } else {
                updatedAtLabel.setText("Last updated: N/A");
            }
        } else {
            errorLabel.setText("No user logged in");
        }
    }

    // UPDATE PROFILE

    @FXML
    private void handleUpdateProfile() {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            errorLabel.setText("Please fill in all fields");
            return;
        }

        try {
            // Get current user and update fields
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser == null) {
                errorLabel.setText("No user logged in");
                return;
            }

            currentUser.setFullName(fullName);
            currentUser.setEmail(email);
            currentUser.setPhoneNumber(phone);

            Map<String,Object> result = UserService.updateProfile(currentUser);
            User updatedUser = (User)result.get("user");
            SessionManager.setCurrentUser(updatedUser);

            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText((String) result.get("message"));
            alert.showAndWait();

            loadUserProfile();

            errorLabel.setText("");

        } catch (Exception e) {
            errorLabel.setText("Update failed: " + e.getMessage());
        }
    }

    // CHANGE PASSWORD

    @FXML
    private void handleChangePassword() {
        String oldPassword = oldPasswordField.getText().trim();
        String newPassword = newPasswordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            errorLabel.setText("Please fill in all password fields");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            errorLabel.setText("New passwords do not match");
            return;
        }

        if (newPassword.length() < 6) {
            errorLabel.setText("New password must be at least 6 characters");
            return;
        }

        try {
            UserService.changePassword(oldPassword, newPassword);

            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Password changed successfully!");
            alert.showAndWait();

            // Clear password fields
            oldPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
            errorLabel.setText("");

        } catch (Exception e) {
            errorLabel.setText("Password change failed: " + e.getMessage());
        }
    }
    // ============================================
    // NAVIGATION
    // ============================================

    @FXML
    private void goToHome() {
        NavigationUtil.goToHome();
    }

    @FXML
    private void handleLogout() {
        NavigationUtil.logout();
    }

}
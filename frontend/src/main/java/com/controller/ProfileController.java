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

    // ============================================
    // UPDATE PROFILE
    // ============================================

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

            // TODO: Call backend update API when available
            // UserService.updateUser(currentUser);

            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Profile updated successfully!");
            alert.showAndWait();

            errorLabel.setText("");

        } catch (Exception e) {
            errorLabel.setText("Update failed: " + e.getMessage());
        }
    }

    // ============================================
    // CHANGE PASSWORD
    // ============================================

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
            // TODO: Call backend change password API when available
            // AuthService.changePassword(oldPassword, newPassword);

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





}
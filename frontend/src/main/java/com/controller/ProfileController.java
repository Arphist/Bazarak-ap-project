package com.controller;

import com.model.User;
import com.service.AuthService;
import com.service.UserService;
import com.util.Config;
import com.util.NavigationUtil;
import com.util.SessionManager;
import com.view.ShowErrorDialog;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ProfileController {

    // FXML FIELDS

    @FXML
    private TextField oldPasswordVisibleField;

    @FXML
    private ToggleButton oldPasswordToggle;

    @FXML
    private TextField newPasswordVisibleField;

    @FXML
    private ToggleButton newPasswordToggle;

    @FXML
    private TextField confirmPasswordVisibleField;

    @FXML
    private ToggleButton confirmPasswordToggle;

    @FXML
    private Label usernameLabel;

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;



    @FXML
    private PasswordField oldPasswordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    // Time information labels
    @FXML
    private Label createdAtLabel;

    @FXML
    private Label updatedAtLabel;

    // Profile photo
    @FXML
    private ImageView profileImageView;

    @FXML
    private Button changePhotoButton;

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
            displayTimeInfo(currentUser);

            // Setup password toggles
            setupPasswordToggle(oldPasswordField, oldPasswordVisibleField, oldPasswordToggle);
            setupPasswordToggle(newPasswordField, newPasswordVisibleField, newPasswordToggle);
            setupPasswordToggle(confirmPasswordField, confirmPasswordVisibleField, confirmPasswordToggle);

            // Display profile photo
            loadProfilePhoto(currentUser);

        } else {
            ShowErrorDialog.showErrorDialog(
                    "Authentication Error",
                    "No user logged in",
                    "Please login first.",
                    "ERROR"
            );
        }
    }

    private void setupPasswordToggle(PasswordField passwordField, TextField visibleField, ToggleButton toggle) {
        // Sync password fields
        passwordField.textProperty().addListener((obs, old, newVal) -> {
            visibleField.setText(newVal);
        });

        visibleField.textProperty().addListener((obs, old, newVal) -> {
            passwordField.setText(newVal);
        });

        toggle.setTooltip(new javafx.scene.control.Tooltip("Show/Hide Password"));
    }

    // TOGGLE METHODS
    @FXML
    private void toggleOldPasswordVisibility() {
        togglePasswordField(oldPasswordField, oldPasswordVisibleField, oldPasswordToggle);
    }

    @FXML
    private void toggleNewPasswordVisibility() {
        togglePasswordField(newPasswordField, newPasswordVisibleField, newPasswordToggle);
    }

    @FXML
    private void toggleConfirmPasswordVisibility() {
        togglePasswordField(confirmPasswordField, confirmPasswordVisibleField, confirmPasswordToggle);
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

    private void displayTimeInfo(User user) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        if (user.getCreatedAt() != null) {
            createdAtLabel.setText("Account created: " + user.getCreatedAt().format(formatter));
        } else {
            createdAtLabel.setText("Account created: N/A");
        }

        if (user.getUpdatedAt() != null) {
            updatedAtLabel.setText("Last updated: " + user.getUpdatedAt().format(formatter));
        } else {
            updatedAtLabel.setText("Last updated: N/A");
        }
    }

    private void loadProfilePhoto(User user) {
        if (user.getProfilePhoto() != null && !user.getProfilePhoto().isEmpty()) {
            try {
                // Construct full URL
                String photoUrl = Config.BASE_IMAGE_URL + user.getProfilePhoto();
                Image image = new Image(photoUrl, true);
                profileImageView.setImage(image);
                profileImageView.setPreserveRatio(true);
                profileImageView.setFitWidth(100);
                profileImageView.setFitHeight(100);
            } catch (Exception e) {
                // If image fails to load, use default
                setDefaultProfilePhoto();
            }
        } else {
            setDefaultProfilePhoto();
        }
    }

    private void setDefaultProfilePhoto() {
        profileImageView.setImage(null);
        profileImageView.setStyle("-fx-background-color: #bdc3c7; -fx-background-radius: 50;");
    }

    // UPDATE PROFILE PHOTO

    @FXML
    private void handleChangeProfilePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.webp")
        );

        Stage stage = (Stage) changePhotoButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                // Call UserService.updateProfilePhoto()
                Map<String, Object> result = UserService.updateProfilePhoto(selectedFile);
                String message = (String) result.get("message");
                String photoUrl = (String) result.get("photoUrl");

                // Show success message
                ShowErrorDialog.showErrorDialog(
                        "Success",
                        null,
                        message,
                        "INFORMATION"
                );

                // Reload profile to show new photo
                loadUserProfile();


            } catch (Exception e) {
                ShowErrorDialog.showErrorDialog(
                        "Profile Photo Error",
                        "Failed to update profile photo",
                        e.getMessage(),
                        "ERROR"
                );
            }
        }
    }

    // UPDATE PROFILE

    @FXML
    private void handleUpdateProfile() {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            ShowErrorDialog.showErrorDialog(
                    "Validation Error",
                    "Incomplete information",
                    "Please fill in all fields.",
                    "WARNING"
            );
            return;
        }

        try {
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser == null) {
                ShowErrorDialog.showErrorDialog(
                        "Authentication Error",
                        "No user logged in",
                        "Please login first.",
                        "ERROR"
                );
                return;
            }

            currentUser.setFullName(fullName);
            currentUser.setEmail(email);
            currentUser.setPhoneNumber(phone);

            // Call backend to update profile
            Map<String, Object> result = UserService.updateProfile(currentUser);
            User updatedUser = (User) result.get("user");

            // Update session with new user
            SessionManager.setCurrentUser(updatedUser);

            ShowErrorDialog.showErrorDialog(
                    "Success",
                    null,
                    (String) result.get("message"),
                    "INFORMATION"
            );

            // Reload profile to show updated data
            loadUserProfile();

        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Profile Error",
                    "Update failed",
                    e.getMessage(),
                    "ERROR"
            );
        }
    }

    // CHANGE PASSWORD

    @FXML
    private void handleChangePassword() {
        String oldPassword = oldPasswordField.getText().trim();
        String newPassword = newPasswordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            ShowErrorDialog.showErrorDialog(
                    "Validation Error",
                    "Incomplete information",
                    "Please fill in all password fields.",
                    "WARNING"
            );
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            ShowErrorDialog.showErrorDialog(
                    "Validation Error",
                    "Password mismatch",
                    "New passwords do not match.",
                    "WARNING"
            );
            return;
        }

        try {
            // Call backend to change password
            String result = UserService.changePassword(oldPassword, newPassword);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText(result);
            alert.showAndWait();

            // Clear password fields
            oldPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();

        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Password Error",
                    "Password change failed",
                    e.getMessage(),
                    "ERROR"
            );
        }
    }

    // NAVIGATION

    @FXML
    private void goToHome() {
        NavigationUtil.goToHome();
    }

    @FXML
    private void handleLogout() {
        try {
            AuthService.logout();
            NavigationUtil.goToLogin();
        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Logout Error",
                    "Logout failed",
                    e.getMessage(),
                    "ERROR"
            );
        }
    }
}
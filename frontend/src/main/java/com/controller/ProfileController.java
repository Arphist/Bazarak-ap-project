package com.controller;

import com.BazarakFrontendApplication;
import com.model.User;
import com.service.AuthService;
import com.service.UserService;
import com.util.Config;
import com.util.NavigationUtil;
import com.util.SessionManager;
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

            // Display profile photo
            loadProfilePhoto(currentUser);

        } else {
            errorLabel.setText("No user logged in");
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
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();

                // Reload profile to show new photo
                loadUserProfile();

                errorLabel.setText("");

            } catch (Exception e) {
                errorLabel.setText("Failed to update profile photo: " + e.getMessage());
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
            errorLabel.setText("Please fill in all fields");
            return;
        }

        try {
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser == null) {
                errorLabel.setText("No user logged in");
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

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText((String) result.get("message"));
            alert.showAndWait();

            // Reload profile to show updated data
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
            errorLabel.setText("");

        } catch (Exception e) {
            errorLabel.setText("Password change failed: " + e.getMessage());
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
            errorLabel.setText("Logout failed: " + e.getMessage());
        }
    }
}
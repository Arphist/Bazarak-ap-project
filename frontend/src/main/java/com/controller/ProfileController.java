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

    @FXML
    private TextField oldPasswordVisibleField;

    @FXML
    private ToggleButton oldPasswordToggle;

    @FXML
    private TextField newPasswordVisibleField;

    @FXML
    private Label defaultAvatarLabel;

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

    @FXML
    private Label createdAtLabel;

    @FXML
    private Label updatedAtLabel;

    @FXML
    private ImageView profileImageView;

    @FXML
    private Button changePhotoButton;

    @FXML
    private void initialize() {
        loadUserProfile();
    }

    private void loadUserProfile() {
        try {
            User currentUser = UserService.getMyProfile();
            if (currentUser != null) {
                System.out.println("📸 Profile photo from backend: " + currentUser.getProfilePhoto());
                usernameLabel.setText(currentUser.getUsername());
                fullNameField.setText(currentUser.getFullName());
                emailField.setText(currentUser.getEmail());
                phoneField.setText(currentUser.getPhoneNumber());

                displayTimeInfo(currentUser);
                setupPasswordToggle(oldPasswordField, oldPasswordVisibleField, oldPasswordToggle);
                setupPasswordToggle(newPasswordField, newPasswordVisibleField, newPasswordToggle);
                setupPasswordToggle(confirmPasswordField, confirmPasswordVisibleField, confirmPasswordToggle);
                loadProfilePhoto(currentUser);
            }
        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Load User Profile Error",
                    "Failed to load your profile",
                    "There was a problem loading your profile. Please try again later.",
                    "ERROR"
            );
        }
    }

    private void setupPasswordToggle(PasswordField passwordField, TextField visibleField, ToggleButton toggle) {
        passwordField.textProperty().addListener((obs, old, newVal) -> visibleField.setText(newVal));
        visibleField.textProperty().addListener((obs, old, newVal) -> passwordField.setText(newVal));
        toggle.setTooltip(new javafx.scene.control.Tooltip("Show/Hide Password"));
    }

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
        createdAtLabel.setText("Account created: " + (user.getCreatedAt() != null ? user.getCreatedAt().format(formatter) : "N/A"));
        updatedAtLabel.setText("Last updated: " + (user.getUpdatedAt() != null ? user.getUpdatedAt().format(formatter) : "N/A"));
    }

    private void loadProfilePhoto(User user) {
        if (user.getProfilePhoto() != null && !user.getProfilePhoto().isEmpty()) {
            try {
                String photoPath = user.getProfilePhoto();
                if (photoPath.startsWith("/")) photoPath = photoPath.substring(1);

                String photoUrl = Config.BASE_IMAGE_URL + "/" + photoPath + "?t=" + System.currentTimeMillis();
                System.out.println("📸 Loading photo from URL: " + photoUrl);

                Image image = new Image(photoUrl, true);

                // When the image loads, center it
                image.progressProperty().addListener((obs, old, progress) -> {
                    if (progress.equals(1.0)  && image.getWidth() > 0 && image.getHeight() > 0) {
                        // Calculate center crop
                        double w = image.getWidth();
                        double h = image.getHeight();
                        double size = Math.min(w, h);
                        double x = (w - size) / 2;
                        double y = (h - size) / 2;

                        profileImageView.setViewport(new javafx.geometry.Rectangle2D(x, y, size, size));
                        System.out.println("📐 Centered: " + w + "x" + h + " → crop " + size + "x" + size);
                    }
                });

                // Clear previous viewport and set image
                profileImageView.setViewport(null);
                profileImageView.setImage(image);
                profileImageView.setPreserveRatio(true);
                profileImageView.setFitWidth(150);
                profileImageView.setFitHeight(150);
                profileImageView.setStyle("");

                defaultAvatarLabel.setVisible(false);
                defaultAvatarLabel.setManaged(false);

            } catch (Exception e) {
                setDefaultProfilePhoto();
            }
        } else {
            setDefaultProfilePhoto();
        }
    }

    private void setDefaultProfilePhoto() {
        profileImageView.setImage(null);
        defaultAvatarLabel.setVisible(true);
        defaultAvatarLabel.setManaged(true);
        profileImageView.setStyle("-fx-background-color: #bdc3c7; -fx-background-radius: 75; -fx-opacity: 0.3;");
    }

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
                Map<String, Object> result = UserService.updateProfilePhoto(selectedFile);
                String message = (String) result.get("message");
                String photoUrl = (String) result.get("photoUrl");

                if (photoUrl != null && !photoUrl.isEmpty()) {
                    // ✅ UPDATE SESSION USER IMMEDIATELY
                    User currentUser = SessionManager.getCurrentUser();
                    if (currentUser != null) {
                        // Store with leading slash for consistency
                        String storePath = photoUrl.startsWith("/") ? photoUrl : "/" + photoUrl;
                        currentUser.setProfilePhoto(storePath);
                        SessionManager.setCurrentUser(currentUser);
                        System.out.println("✅ Updated session user with new photo: " + storePath);

                        // ✅ RELOAD THE PHOTO IMMEDIATELY using the updated user
                        loadProfilePhoto(currentUser);
                    }
                }

                ShowErrorDialog.showErrorDialog("Success", null, message, "INFORMATION");

            } catch (Exception e) {
                e.printStackTrace();
                ShowErrorDialog.showErrorDialog(
                        "Profile Photo Error",
                        "Failed to update profile photo",
                        e.getMessage(),
                        "ERROR"
                );
            }
        }
    }

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

            Map<String, Object> result = UserService.updateProfile(currentUser);
            User updatedUser = (User) result.get("user");

            SessionManager.setCurrentUser(updatedUser);

            ShowErrorDialog.showErrorDialog(
                    "Success",
                    null,
                    (String) result.get("message"),
                    "INFORMATION"
            );

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
            String result = UserService.changePassword(oldPassword, newPassword);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText(result);
            alert.showAndWait();

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

    @FXML
    private void goToHome() {
        NavigationUtil.goToHome();
    }

    @FXML
    private void goBack() {
        NavigationUtil.goBack();
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
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
import javafx.stage.Stage;

public class LoginController {

    // ======== FXML FIELDS ========

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

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
            BazarakFrontendApplication.showHomePage();

        } catch (Exception e) {
            errorLabel.setText("Login failed: " + e.getMessage());
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

    // ======== NAVIGATION ========

    @FXML
    private void goToRegister() {
        NavigationUtil.goToRegister();
    }





}
package com.controller;

import com.model.User;
import com.service.AuthService;
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

    // ======== STAGE ========

    private Stage primaryStage;

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
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
            goToMainPage();

        } catch (Exception e) {
            errorLabel.setText("Login failed: " + e.getMessage());
        }
    }

    // ======== NAVIGATION ========

    @FXML
    private void goToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/register.fxml"));
            Scene scene = new Scene(loader.load(), 400, 500);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Register - Bazarak");

            RegisterController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void goToMainPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Bazarak - Secondhand Marketplace");

            MainController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
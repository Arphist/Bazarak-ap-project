package com;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class BazarakFrontendApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Window title
        primaryStage.setTitle("Bazarak - Second Hand Marketplace");

        // Create UI components
        Label titleLabel = new Label("🪙 Welcome to Bazarak!");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label subtitleLabel = new Label("Second Hand Marketplace");
        subtitleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");

        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px 30px;");
        loginButton.setOnAction(e -> {
            System.out.println("Login button clicked!");
        });

        Button registerButton = new Button("Register");
        registerButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px 30px;");
        registerButton.setOnAction(e -> {
            System.out.println("Register button clicked!");
        });

        Button exitButton = new Button("Exit");
        exitButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px 30px; -fx-background-color: #ff4444; -fx-text-fill: white;");
        exitButton.setOnAction(e -> {
            System.out.println("Exiting Bazarak...");
            primaryStage.close();
        });

        // Layout
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(40));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(titleLabel, subtitleLabel, loginButton, registerButton, exitButton);

        // Scene
        Scene scene = new Scene(layout, 500, 400);
        primaryStage.setScene(scene);
        primaryStage.show();

        System.out.println("========================================");
        System.out.println("  🪙  Bazarak Frontend is running!  🪙  ");
        System.out.println("  Second-hand marketplace client      ");
        System.out.println("========================================");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
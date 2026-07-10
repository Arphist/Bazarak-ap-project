package com;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BazarakFrontendApplication extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        stage.setTitle("Bazarak - Second Hand Marketplace");
        stage.setMinWidth(1024);
        stage.setMinHeight(768);
        stage.setResizable(true);

        // Start with Login Page
        showLoginPage();

        stage.show();

        System.out.println("========================================");
        System.out.println("  🪙  Bazarak Frontend is running!  🪙  ");
        System.out.println("  Second-hand marketplace client      ");
        System.out.println("========================================");
    }

    // PAGE NAVIGATION METHODS

    public static void showLoginPage() {
        loadPage("/view/login.fxml", "Bazarak - Login");
    }

    public static void showRegisterPage() {
        loadPage("/view/register.fxml", "Bazarak - Register");
    }

    public static void showHomePage() {
        loadPage("/view/home.fxml", "Bazarak - Home");
    }

    public static void showAdDetailsPage(Long adId) {
        // We'll pass adId via controller later
        loadPage("/view/ad-details.fxml", "Bazarak - Ad Details");
    }

    public static void showPostAdPage() {
        loadPage("/view/post-ad.fxml", "Bazarak - Post Ad");
    }

    public static void showMyAdsPage() {
        loadPage("/view/my-ads.fxml", "Bazarak - My Ads");
    }

    public static void showFavoritesPage() {
        loadPage("/view/favorites.fxml", "Bazarak - Favorites");
    }

    public static void showChatPage() {
        loadPage("/view/chat.fxml", "Bazarak - Chat");
    }

    public static void showProfilePage() {
        loadPage("/view/profile.fxml", "Bazarak - Profile");
    }

    public static void showAdminDashboard() {
        loadPage("/view/admin-dashboard.fxml", "Bazarak - Admin Dashboard");
    }

    // HELPER METHODS

    private static void loadPage(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    BazarakFrontendApplication.class.getResource(fxmlPath)
            );
            Parent root = loader.load();
            primaryStage.setTitle(title);
            primaryStage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading page: " + fxmlPath);
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
package com;

import com.util.DataHolder;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BazarakFrontendApplication extends Application {

    private static Stage primaryStage;
    private static Scene currentScene;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        stage.setTitle("Bazarak - Second Hand Marketplace");
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.setResizable(true);

        FXMLLoader loader = new FXMLLoader(
                BazarakFrontendApplication.class.getResource("/view/login.fxml")
        );
        Parent root = loader.load();
        currentScene = new Scene(root, 1024, 768);
        stage.setScene(currentScene);
        stage.show();

        System.out.println("========================================");
        System.out.println("     Bazarak Frontend is running!      ");
        System.out.println("    Second-hand marketplace client      ");
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

    public static void showAdDetailsPage() {
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

    public static void showRatingPage() {loadPage("/view/rating.fxml", "Bazarak - Rate Seller");}

    // HELPER METHODS

    private static void loadPage(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    BazarakFrontendApplication.class.getResource(fxmlPath)
            );
            Parent root = loader.load();
            currentScene.setRoot(root);
            primaryStage.setTitle(title);

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
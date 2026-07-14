package com.util;

import com.BazarakFrontendApplication;

/**
 * Utility class for navigation between pages
 * Centralizes all navigation logic in one place
 */
public class NavigationUtil {

    // NAVIGATION METHODS

    /**
     * Navigate to the login page
     */
    public static void goToLogin() {
        BazarakFrontendApplication.showLoginPage();
    }

    /**
     * Navigate to the register page
     */
    public static void goToRegister() {
        BazarakFrontendApplication.showRegisterPage();
    }

    /**
     * Navigate to the home page
     */
    public static void goToHome() {
        BazarakFrontendApplication.showHomePage();
    }

    /**
     * Navigate to the create ad page
     */
    public static void goToCreateAd() {
        BazarakFrontendApplication.showPostAdPage();
    }

    /**
     * Navigate to the favorites page
     */
    public static void goToFavorites() {
        BazarakFrontendApplication.showFavoritesPage();
    }

    /**
     * Navigate to the profile page
     */
    public static void goToProfile() {
        BazarakFrontendApplication.showProfilePage();
    }

    /**
     * Navigate to the admin dashboard
     */
    public static void goToAdminDashboard() {
        BazarakFrontendApplication.showAdminDashboard();
    }

    /**
     * Navigate to ad details page
     */
    public static void goToAdDetails(Long adId) {
        BazarakFrontendApplication.showAdDetailsPage(adId);
    }

    /**
     * Navigate to chat page
     */
    public static void goToChat() {
        BazarakFrontendApplication.showChatPage();
    }

    /**
     * Navigate to my ads page
     */
    public static void goToMyAds() {
        BazarakFrontendApplication.showMyAdsPage();
    }

    /**
     * Navigate back to the previous page (if needed)
     * This requires a history stack implementation
     */
    public static void goBack() {
        // You can implement a history stack here
        // For now, just go to home
        goToHome();
    }

    /**
     * Logout user and navigate to login page
     */
    public static void logout() {
        try {
            com.service.AuthService.logout();
            goToLogin();
        } catch (Exception e) {
            System.err.println("Logout failed: " + e.getMessage());
            goToLogin(); // Always go to login even if logout fails
        }
    }
}
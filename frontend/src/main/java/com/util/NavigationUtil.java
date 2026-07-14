package com.util;

import com.BazarakFrontendApplication;

/**
 * Utility class for navigation between pages
 * Centralizes all navigation logic in one place
 */
public class NavigationUtil {
    // PAGE NAMES (Constants)
    public static final String PAGE_LOGIN = "LOGIN";
    public static final String PAGE_REGISTER = "REGISTER";
    public static final String PAGE_HOME = "HOME";
    public static final String PAGE_CREATE_AD = "CREATE_AD";
    public static final String PAGE_FAVORITES = "FAVORITES";
    public static final String PAGE_PROFILE = "PROFILE";
    public static final String PAGE_ADMIN = "ADMIN";
    public static final String PAGE_AD_DETAILS = "AD_DETAILS";
    public static final String PAGE_CHAT = "CHAT";
    public static final String PAGE_MY_ADS = "MY_ADS";

    // PRIVATE HELPER: Navigate with History

    private static void navigateTo(String pageName, Runnable navigationAction) {
        // Push current page to history BEFORE navigating
        // Except for Login and Register pages (shouldn't be in history)
        if (!pageName.equals(PAGE_LOGIN) && !pageName.equals(PAGE_REGISTER)) {
            String currentPage = HistoryManager.getCurrentPage();
            // Only push if we're not already on this page
            if (currentPage == null || !currentPage.equals(pageName)) {
                HistoryManager.pushPage(pageName);
            }
        }
        // Execute the actual navigation
        navigationAction.run();
    }


    // NAVIGATION METHODS

    /**
     * Navigate to the login page
     */
    public static void goToLogin() {
        HistoryManager.clearHistory(); // Clear history on logout/login
        navigateTo(PAGE_LOGIN, BazarakFrontendApplication::showLoginPage);
    }

    /**
     * Navigate to the register page
     */
    public static void goToRegister() {
        // Didn't add register to history (users shouldn't go back to register)
        BazarakFrontendApplication.showRegisterPage();
    }

    /**
     * Navigate to the home page
     */
    public static void goToHome() {
        navigateTo(PAGE_HOME, BazarakFrontendApplication::showHomePage);
    }

    /**
     * Navigate to the create ad page
     */
    public static void goToCreateAd() {
        navigateTo(PAGE_CREATE_AD, BazarakFrontendApplication::showPostAdPage);
    }

    /**
     * Navigate to the favorites page
     */
    public static void goToFavorites() {
        navigateTo(PAGE_FAVORITES, BazarakFrontendApplication::showFavoritesPage);
    }

    /**
     * Navigate to the profile page
     */
    public static void goToProfile() {
        navigateTo(PAGE_PROFILE, BazarakFrontendApplication::showProfilePage);
    }

    /**
     * Navigate to the admin dashboard
     */
    public static void goToAdminDashboard() {
        navigateTo(PAGE_ADMIN, BazarakFrontendApplication::showAdminDashboard);
    }

    /**
     * Navigate to ad details page
     */
    public static void goToAdDetails(Long adId) {
        // Store ad ID in DataHolder
        DataHolder.setSelectedAdId(adId);
        navigateTo(PAGE_AD_DETAILS, BazarakFrontendApplication::showAdDetailsPage);
    }

    /**
     * Navigate to chat page
     */
    public static void goToChat() {
        navigateTo(PAGE_CHAT, BazarakFrontendApplication::showChatPage);
    }

    /**
     * Navigate to my ads page
     */
    public static void goToMyAds() {
        navigateTo(PAGE_MY_ADS, BazarakFrontendApplication::showMyAdsPage);
    }

    // GO BACK (Using History Stack)

    /**
     * Navigate back to the previous page (if needed)
     * This requires a history stack implementation
     * <p>
     * Navigate back to the previous page
     */
    public static void goBack() {
        if (!HistoryManager.hasPreviousPage()) {
            // No history, go to home
            goToHome();
            return;
        }

        // Get the previous page from history
        String previousPage = HistoryManager.goBack();

        // Navigate to the previous page
        switch (previousPage) {
            case PAGE_HOME:
                BazarakFrontendApplication.showHomePage();
                break;
            case PAGE_CREATE_AD:
                BazarakFrontendApplication.showPostAdPage();
                break;
            case PAGE_FAVORITES:
                BazarakFrontendApplication.showFavoritesPage();
                break;
            case PAGE_PROFILE:
                BazarakFrontendApplication.showProfilePage();
                break;
            case PAGE_ADMIN:
                BazarakFrontendApplication.showAdminDashboard();
                break;
            case PAGE_AD_DETAILS:
                BazarakFrontendApplication.showAdDetailsPage(DataHolder.getSelectedAdId());
                break;
            case PAGE_CHAT:
                BazarakFrontendApplication.showChatPage();
                break;
            case PAGE_MY_ADS:
                BazarakFrontendApplication.showMyAdsPage();
                break;
            case PAGE_LOGIN:
            default:
                goToHome();
                break;
        }
    }

    /**
     * Logout user and navigate to login page
     */
    public static void logout() {
        try {
            com.service.AuthService.logout();
            HistoryManager.clearHistory();
            goToLogin();
        } catch (Exception e) {
            System.err.println("Logout failed: " + e.getMessage());
            HistoryManager.clearHistory();
            goToLogin();
        }
    }

}
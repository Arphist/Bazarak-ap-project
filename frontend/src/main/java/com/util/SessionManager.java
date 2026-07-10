package com.util;

import com.model.User;

public class SessionManager {
    private static User currentUser;
    private static String authToken;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static String getAuthToken() {
        return authToken;
    }

    public static void setAuthToken(String token) {
        authToken = token;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public static void clearSession() {
        currentUser = null;
        authToken = null;
    }
}
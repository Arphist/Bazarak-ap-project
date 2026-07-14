package com.util;


import java.util.Stack;

/**
 * Manages navigation history for the application
 * Implements a stack-based history (LIFO - Last In, First Out)
 */
public class HistoryManager {

    private static final Stack<String> historyStack = new Stack<>();
    private static final int MAX_HISTORY_SIZE = 50; // Prevent memory issues

    /**
     * Push a new page to the history stack
     * Called whenever a new page is navigated to
     */
    public static void pushPage(String pageName) {
        // If the same page is already on top, don't push again
        if (!historyStack.isEmpty() && historyStack.peek().equals(pageName)) {
            return;
        }

        historyStack.push(pageName);

        // Trim history if it gets too large
        while (historyStack.size() > MAX_HISTORY_SIZE) {
            historyStack.remove(0);
        }

        System.out.println("History: " + historyStack);
    }

    /**
     * Get the previous page from history
     * Removes the current page from stack
     */
    public static String goBack() {
        if (historyStack.isEmpty()) {
            return "HOME"; // Default fallback
        }

        // Remove current page (top of stack)
        historyStack.pop();

        // Get the previous page (new top)
        if (historyStack.isEmpty()) {
            return "HOME";
        }

        return historyStack.peek();
    }

    /**
     * Get the previous page without removing it (preview)
     */
    public static String peekPreviousPage() {
        if (historyStack.size() < 2) {
            return "HOME";
        }
        // Stack: [Login, Home, Profile]
        // size=3, index=1 → Home
        return historyStack.get(historyStack.size() - 2);
    }

    /**
     * Clear the entire history
     * Used on logout
     */
    public static void clearHistory() {
        historyStack.clear();
        System.out.println("History cleared");
    }

    /**
     * Check if there is a previous page
     */
    public static boolean hasPreviousPage() {
        return historyStack.size() >= 2;
    }

    /**
     * Get the size of the history stack
     */
    public static int getHistorySize() {
        return historyStack.size();
    }

    /**
     * Get the current page (top of stack)
     */
    public static String getCurrentPage() {
        if (historyStack.isEmpty()) {
            return null;
        }
        return historyStack.peek();
    }
}
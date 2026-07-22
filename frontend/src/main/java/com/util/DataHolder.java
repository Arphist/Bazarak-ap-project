package com.util;

import com.model.User;

/**
 * Holds temporary data passed between pages
 * Used for passing data like ad ID to detail page
 */
public class DataHolder {
    private static Long selectedAdId;
    private static Long selectedConversationId;
    private static Long selectedUserId;
    private static User selectedUser;

    // AD ID

    public static void setSelectedAdId(Long id) {
        selectedAdId = id;
    }

    public static Long getSelectedAdId() {
        return selectedAdId;
    }

    public static void clearSelectedAdId() {
        selectedAdId = null;
    }

    // CONVERSATION ID

    public static void setSelectedConversationId(Long id) {
        selectedConversationId = id;
    }

    public static Long getSelectedConversationId() {
        return selectedConversationId;
    }

    public static void clearSelectedConversationId() {
        selectedConversationId = null;
    }

    // USER ID

    public static void setSelectedUserId(Long id) {
        selectedUserId = id;
    }

    public static Long getSelectedUserId() {
        return selectedUserId;
    }

    public static void clearSelectedUserId() {
        selectedUserId = null;
    }

    public static User getSelectedUser() {
        return selectedUser;
    }

    public static void setSelectedUser(User selectedUser) {
        DataHolder.selectedUser = selectedUser;
    }

    public static void clearSelectedUser() {
        selectedUser = null;
    }
    // CLEAR ALL

    public static void clearAll() {
        selectedAdId = null;
        selectedConversationId = null;
        selectedUserId = null;
    }
}
package com.view;

import com.model.Conversation;
import com.model.Message;
import com.model.User;
import javafx.scene.control.ListCell;

public class ConversationCell extends ListCell<Conversation> {
    private final User currentUser;

    public ConversationCell(User currentUser) {
        this.currentUser = currentUser;
    }

    @Override
    protected void updateItem(Conversation conv, boolean empty) {
        super.updateItem(conv, empty);
        if (empty || conv == null) {
            setText(null);
        } else {
            // ===== طرف مقابل رو پیدا کن =====
            String otherUsername = conv.getOtherParticipantUsername(currentUser.getId());

            // ===== آخرین پیام رو بگیر =====
            Message latestMessage = conv.getLatestMessage();

            StringBuilder display = new StringBuilder(otherUsername);
            if (latestMessage != null) {
                String preview = latestMessage.getContent();
                if (preview.length() > 40) {
                    preview = preview.substring(0, 40) + "...";
                }
                display.append("\n").append(preview);
            } else {
                display.append("\nNo messages yet");
            }
            setText(display.toString());
        }
    }
}
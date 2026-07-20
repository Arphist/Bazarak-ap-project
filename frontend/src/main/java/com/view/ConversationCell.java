package com.view;


import com.model.Conversation;
import com.model.Message;
import com.model.User;
import javafx.scene.control.ListCell;

public class ConversationCell extends ListCell<Conversation> {
    private final User otherUser;

    public ConversationCell(User otherUser){
        this.otherUser=otherUser;
    }

    @Override
    protected void updateItem(Conversation conv, boolean empty) {
        super.updateItem(conv, empty);
        if (empty || conv == null) {
            setText(null);
        } else {
            // Get latest message (using the model's method)
            Message latestMessage = conv.getLatestMessage();
            // Show other participant's username and latest message preview
            StringBuilder display = new StringBuilder(otherUser.getUsername());
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

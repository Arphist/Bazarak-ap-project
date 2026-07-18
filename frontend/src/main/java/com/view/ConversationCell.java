package com.view;


import com.model.Conversation;
import javafx.scene.control.ListCell;

public class ConversationCell extends ListCell<Conversation> {
    private Long userId;

    public ConversationCell(Long userId){
        this.userId=userId;
    }

    @Override
    protected void updateItem(Conversation conv, boolean empty) {
        super.updateItem(conv, empty);
        if (empty || conv == null) {
            setText(null);
        } else {
            // Show other participant's username and latest message preview
            String display = conv.getOtherParticipantUsername(userId);
            if (conv.getLatestMessage() != null) {
                display += " - " + conv.getLatestMessage().getContent();
            }
            setText(display);
        }
    }
}

package com.view;

import com.model.Message;
import com.model.User;
import javafx.scene.control.ListCell;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class MessageCell extends ListCell<Message> {
    private User user;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public MessageCell(User user) {
        this.user = user;
    }

    @Override
    protected void updateItem(Message message, boolean empty) {
        super.updateItem(message, empty);
        if (empty || message == null) {
            setText(null);
            setStyle("");
        } else {
            boolean isMine = message.getSender() != null &&
                    user != null &&
                    message.getSender().getId().equals(user.getId());

            String sender = isMine ? "You" : message.getSender().getUsername();
            String time = message.getSentAt() != null ?
                    message.getSentAt().format(timeFormatter) : "";

            setText(sender + " (" + time + "): " + message.getContent());

            // Style differently for sent vs received
            if (isMine) {
                setStyle("-fx-background-color: #dcf8c6; -fx-padding: 5; -fx-background-radius: 5;");
            } else {
                setStyle("-fx-background-color: #ffffff; -fx-padding: 5; -fx-background-radius: 5;");
            }
        }
    }

}

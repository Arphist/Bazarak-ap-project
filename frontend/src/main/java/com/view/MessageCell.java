package com.view;

import com.model.Message;
import com.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.time.format.DateTimeFormatter;

public class MessageCell extends ListCell<Message> {

    private final User currentUser;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public MessageCell(User currentUser) {
        this.currentUser = currentUser;
    }

    @Override
    protected void updateItem(Message message, boolean empty) {
        super.updateItem(message, empty);

        if (empty || message == null || message.getSender() == null) {
            setGraphic(null);
            setText(null);
            return;
        }

        boolean isMine = currentUser != null &&
                message.getSender().getId().equals(currentUser.getId());

        // content
        Label contentLabel = new Label(message.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(400);
        contentLabel.setPadding(new Insets(8, 12, 8, 12));

        // time
        Label timeLabel = new Label(
                message.getSentAt() != null ?
                        message.getSentAt().format(timeFormatter) : ""
        );
        timeLabel.setStyle("-fx-text-fill: #9E9E9E; -fx-font-size: 10px;");

        // sender name
        Label senderLabel = new Label(message.getSender().getUsername());
        senderLabel.setStyle("-fx-text-fill: #BB86FC; -fx-font-size: 11px;");

        VBox messageBox = new VBox(2);
        HBox messageContainer = new HBox();

        if (isMine) {
            // ===== my message =====
            contentLabel.setStyle(
                    "-fx-background-color: #2A4A7F; " +
                            "-fx-text-fill: white; " +
                            "-fx-background-radius: 12 12 0 12;"
            );
            messageBox.getChildren().addAll(contentLabel, timeLabel);
            messageBox.setAlignment(Pos.CENTER_RIGHT);
            messageContainer.setAlignment(Pos.CENTER_RIGHT);
            messageContainer.getChildren().add(messageBox);
            setStyle("-fx-padding: 4 10 4 10;");

        } else {
            // ===== others message =====
            contentLabel.setStyle(
                    "-fx-background-color: #3A3A3A; " +
                            "-fx-text-fill: white; " +
                            "-fx-background-radius: 12 12 12 0;"
            );
            messageBox.getChildren().addAll(senderLabel, contentLabel, timeLabel);
            messageBox.setAlignment(Pos.CENTER_LEFT);
            messageContainer.setAlignment(Pos.CENTER_LEFT);
            messageContainer.getChildren().add(messageBox);
            setStyle("-fx-padding: 4 10 4 10;");
        }

        setGraphic(messageContainer);
    }
}
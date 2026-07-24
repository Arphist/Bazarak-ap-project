package com.view;

import com.model.Conversation;
import com.model.Message;
import com.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.time.format.DateTimeFormatter;

public class ConversationCell extends ListCell<Conversation> {

    private final User currentUser;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    // ============================================
    // UI COMPONENTS
    // ============================================

    private final Label avatarLabel = new Label();
    private final Circle avatarCircle = new Circle(20);
    private final Label usernameLabel = new Label();
    private final Label lastMessageLabel = new Label();
    private final Label timeLabel = new Label();

    public ConversationCell(User currentUser) {
        this.currentUser = currentUser;
        createUI();
    }

    // ============================================
    // BUILD UI
    // ============================================

    private void createUI() {
        // Avatar setup
        avatarLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
        avatarLabel.setAlignment(Pos.CENTER);
        avatarLabel.setPrefWidth(40);
        avatarLabel.setPrefHeight(40);

        avatarCircle.setFill(Color.web("#4A6FA5"));
        avatarCircle.setStroke(Color.web("#2C3E50"));
        avatarCircle.setStrokeWidth(1);

        // Username label
        usernameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #FFFFFF;");

        // Last message preview
        lastMessageLabel.setStyle("-fx-text-fill: #9E9E9E; -fx-font-size: 12px;");

        // Timestamp
        timeLabel.setStyle("-fx-text-fill: #6C757D; -fx-font-size: 11px;");
        timeLabel.setAlignment(Pos.CENTER_RIGHT);

        // Layout container for avatar
        VBox avatarBox = new VBox(avatarLabel);
        avatarBox.setAlignment(Pos.CENTER);
        avatarBox.setPrefWidth(50);
        avatarBox.setPrefHeight(50);

        avatarLabel.setGraphic(avatarCircle);
        avatarLabel.setContentDisplay(ContentDisplay.CENTER);
    }

    // ============================================
    // UPDATE ITEM
    // ============================================

    @Override
    protected void updateItem(Conversation conv, boolean empty) {
        super.updateItem(conv, empty);

        if (empty || conv == null || currentUser == null) {
            setGraphic(null);
            setText(null);
            return;
        }

        // ===== Get the other participant =====
        String otherUsername = conv.getOtherParticipantUsername(currentUser.getId());
        String initial = otherUsername.isEmpty() ? "?" : otherUsername.substring(0, 1).toUpperCase();

        // ===== Update avatar =====
        avatarLabel.setText(initial);
        avatarCircle.setFill(getAvatarColor(otherUsername));

        // ===== Update username =====
        usernameLabel.setText(otherUsername);

        // ===== Get latest message =====
        Message latest = conv.getLatestMessage();
        if (latest != null) {
            String preview = latest.getContent();
            if (preview.length() > 50) {
                preview = preview.substring(0, 50) + "...";
            }
            lastMessageLabel.setText(preview);
            timeLabel.setText(latest.getSentAt() != null ?
                    latest.getSentAt().format(timeFormatter) : "");
        } else {
            lastMessageLabel.setText("No messages yet");
            timeLabel.setText("");
        }

        // ===== Build and display the cell =====
        setGraphic(buildCellLayout());
    }

    // ============================================
    // BUILD CELL LAYOUT
    // ============================================

    private HBox buildCellLayout() {
        // Left: Avatar
        VBox avatarBox = new VBox(avatarLabel);
        avatarBox.setAlignment(Pos.CENTER);
        avatarBox.setPrefWidth(50);
        avatarBox.setPrefHeight(50);

        // Center: Username + Last message
        VBox textBox = new VBox(2, usernameLabel, lastMessageLabel);
        textBox.setAlignment(Pos.CENTER_LEFT);
        textBox.setPadding(new Insets(0, 5, 0, 0));

        // Right: Timestamp
        VBox timeBox = new VBox(timeLabel);
        timeBox.setAlignment(Pos.CENTER_RIGHT);

        // Final row
        HBox row = new HBox(10, avatarBox, textBox, timeBox);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 12, 8, 12));
        row.setStyle("-fx-background-color: transparent; -fx-border-color: #2A2A2A; -fx-border-width: 0 0 1 0;");

        return row;
    }

    // ============================================
    // HELPER: COLOR BY USERNAME
    // ============================================

    private Color getAvatarColor(String username) {
        if (username == null || username.isEmpty()) return Color.web("#4A6FA5");
        char c = username.toLowerCase().charAt(0);
        if (c >= 'a' && c <= 'e') return Color.web("#E67E22");
        if (c >= 'f' && c <= 'j') return Color.web("#27AE60");
        if (c >= 'k' && c <= 'o') return Color.web("#2980B9");
        if (c >= 'p' && c <= 't') return Color.web("#8E44AD");
        return Color.web("#C0392B");
    }
}
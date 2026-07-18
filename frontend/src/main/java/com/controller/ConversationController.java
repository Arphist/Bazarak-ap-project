package com.controller;

import com.model.ChatMessage;
import com.model.Conversation;
import com.model.Message;
import com.model.User;
import com.service.ConversationService;
import com.util.NavigationUtil;
import com.util.SessionManager;
import com.websocket.ChatWebSocketClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ConversationController {

    // FXML FIELDS

    @FXML
    private ListView<Conversation> conversationListView;

    @FXML
    private ListView<Message> messageListView;

    @FXML
    private TextArea messageInput;

    @FXML
    private Label errorLabel;

    @FXML
    private Label conversationTitleLabel;

    @FXML
    private VBox chatContainer;

    // DATA

    private ObservableList<Conversation> conversations = FXCollections.observableArrayList();
    private ObservableList<Message> messages = FXCollections.observableArrayList();
    private Conversation currentConversation;
    private ChatWebSocketClient webSocketClient;
    private User currentUser;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    // ============================================
    // INITIALIZE
    // ============================================

    @FXML
    private void initialize() {
        currentUser = SessionManager.getCurrentUser();

        // Setup conversation list
        conversationListView.setItems(conversations);
        conversationListView.setCellFactory(lv -> new ConversationCell());
        conversationListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                Conversation selected = conversationListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    loadConversation(selected);
                }
            }
        });

        // Setup message list
        messageListView.setItems(messages);
        messageListView.setCellFactory(lv -> new MessageCell());

        // Setup send on Enter key
        messageInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !event.isShiftDown()) {
                event.consume();
                sendMessage();
            }
        });
        // Connect WebSocket
        connectWebSocket();

        // Load conversations
        loadConversations();
    }

    // ============================================
    // LOAD CONVERSATIONS
    // ============================================

    private void loadConversations() {
        try {
            List<Conversation> conversationList = ConversationService.getAllConversations();
            conversations.clear();
            conversations.addAll(conversationList);

            if (conversations.isEmpty()) {
                errorLabel.setText("No conversations yet.");
                conversationTitleLabel.setText("No conversation selected");
            } else {
                errorLabel.setText("");
                // Auto-select first conversation
                conversationListView.getSelectionModel().selectFirst();
                loadConversation(conversationListView.getSelectionModel().getSelectedItem());
            }

        } catch (Exception e) {
            errorLabel.setText("Failed to load conversations: " + e.getMessage());
        }
    }

    // ============================================
    // LOAD CONVERSATION
    // ============================================

    private void loadConversation(Conversation conversation) {
        if (conversation == null) return;

        this.currentConversation = conversation;

        try {
            List<Message> messageList = ConversationService.getMessages(conversation.getId());
            messages.clear();
            messages.addAll(messageList);

            // Scroll to bottom
            Platform.runLater(() -> {
                messageListView.scrollTo(messages.size() - 1);
            });

            // Update title with other participant's name
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser != null) {
                try {
                    String otherUser = conversation.getOtherParticipantUsername(currentUser.getId());
                    conversationTitleLabel.setText("Chat with " + otherUser);
                } catch (IllegalStateException e) {
                    conversationTitleLabel.setText("Conversation");
                }
            }

            errorLabel.setText("");

        } catch (Exception e) {
            errorLabel.setText("Failed to load messages: " + e.getMessage());
        }
    }

    // ============================================
    // SEND MESSAGE
    // ============================================


    // TODO: implement send message and its button in fxml file


    // ============================================
    // REFRESH
    // ============================================

    @FXML
    private void refreshConversations() {
        loadConversations();
    }

    // ============================================
    // NAVIGATION
    // ============================================

    @FXML
    private void goToHome() {
        NavigationUtil.goToHome();
    }

    @FXML
    private void goBack() {
        NavigationUtil.goBack();
    }



}
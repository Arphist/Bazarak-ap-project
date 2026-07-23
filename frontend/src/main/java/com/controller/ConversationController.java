package com.controller;

import com.model.ChatMessage;
import com.model.Conversation;
import com.model.Message;
import com.model.User;
import com.service.ConversationService;
import com.util.DataHolder;
import com.util.NavigationUtil;
import com.util.SessionManager;
import com.view.ConversationCell;
import com.view.MessageCell;
import com.view.ShowErrorDialog;
import com.websocket.ChatWebSocketClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.time.LocalDateTime;

public class ConversationController {

    // FXML FIELDS

    @FXML
    private ListView<Conversation> conversationListView;

    @FXML
    private ListView<Message> messageListView;

    @FXML
    private TextArea messageInput;



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

    // INITIALIZE

    @FXML
    private void initialize() {
        currentUser = SessionManager.getCurrentUser();

        // Setup conversation list
        conversationListView.setItems(conversations);
        conversationListView.setCellFactory(lv -> new ConversationCell(currentUser));
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
        messageListView.setCellFactory(lv -> new MessageCell(currentUser));

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

        // Check if we have a specific conversation to load
        Long conversationId = DataHolder.getSelectedConversationId();
        if (conversationId != null) {
            loadSpecificConversation(conversationId);
            DataHolder.clearSelectedConversationId(); // Clear after loading
        }
    }

    private void loadSpecificConversation(Long conversationId) {
        try {
            // Wait for conversations to load, then select the specific one
            Platform.runLater(() -> {
                for (Conversation conv : conversations) {
                    if (conv.getId().equals(conversationId)) {
                        conversationListView.getSelectionModel().select(conv);
                        loadConversation(conv);
                        break;
                    }
                }
            });
        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Conversation Error",
                    "Failed to load conversation",
                    e.getMessage(),
                    "ERROR"
            );
        }
    }

    // WEBSOCKET CONNECTION
    private void connectWebSocket() {
        try {
            Long conversationId = currentConversation != null ? currentConversation.getId() : null;
            if (conversationId != null) {
                webSocketClient = new ChatWebSocketClient(this::handleWebSocketMessage, conversationId);
                webSocketClient.connect();
            }
        } catch (Exception e) {
            System.err.println("Failed to connect WebSocket: " + e.getMessage());
        }
    }

    /**
     * Handle incoming WebSocket messages
     */
    private void handleWebSocketMessage(ChatMessage chatMessage) {
        Platform.runLater(() -> {
            // Check if this message belongs to the current conversation
            if (currentConversation != null &&
                    chatMessage.getConversationId().equals(currentConversation.getId().toString())) {

                // Convert ChatMessage to Message for display
                Message message = new Message();
                message.setId(Long.parseLong(chatMessage.getId()));
                message.setContent(chatMessage.getContent());

                User sender = new User();
                sender.setId(Long.parseLong(chatMessage.getSenderId()));
                sender.setUsername(chatMessage.getSenderUsername());
                message.setSender(sender);

                message.setSentAt(chatMessage.getTimestamp());

                // Add to message list
                messages.add(message);
                messageListView.scrollTo(messages.size() - 1);

                conversationListView.refresh();

                // Scroll to bottom
                messageListView.scrollTo(messages.size() - 1);
            }
        });
    }

    // LOAD CONVERSATIONS

    // Load all conversations
    private void loadConversations() {
        try {
            List<Conversation> conversationList = ConversationService.getAllConversations();
            conversations.clear();
            conversations.addAll(conversationList);

            if (conversations.isEmpty()) {
                ShowErrorDialog.showErrorDialog(
                        "No Conversations",
                        "No conversations found",
                        "You don't have any conversations yet.",
                        "INFORMATION"
                );
                conversationTitleLabel.setText("No conversation selected");
            } else {
                // Auto-select first conversation
                conversationListView.getSelectionModel().selectFirst();
                loadConversation(conversationListView.getSelectionModel().getSelectedItem());
            }

        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Conversation Error",
                    "Failed to load conversations",
                    e.getMessage(),
                    "ERROR"
            );
        }
    }

    // Load a specific conversation
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


        } catch (Exception e) {
            ShowErrorDialog.showErrorDialog(
                    "Message Error",
                    "Failed to load messages",
                    e.getMessage(),
                    "ERROR"
            );
        }
    }

    // SEND MESSAGE

    @FXML
    private void sendMessage() {
        String content = messageInput.getText().trim();
        if (content.isEmpty() || currentConversation == null || currentUser == null) {
            return;
        }

        // SEND VIA WEBSOCKET ONLY
        if (webSocketClient != null && webSocketClient.isConnected()) {
            ChatMessage chatMessage = new ChatMessage(
                    currentConversation.getId().toString(),
                    currentUser.getId().toString(),
                    currentUser.getUsername(),
                    content
            );
            webSocketClient.sendMessage(chatMessage);
            messageInput.clear();
        } else {
            ShowErrorDialog.showErrorDialog(
                    "Connection Error",
                    "Connection lost",
                    "Please refresh.",
                    "ERROR"
            );
        }
    }

    // REFRESH
    @FXML
    private void refreshConversations() {
        loadConversations();
    }

    // NAVIGATION
    @FXML
    private void goToHome() {
        NavigationUtil.goToHome();
    }

    @FXML
    private void goBack() {
        NavigationUtil.goBack();
    }
}
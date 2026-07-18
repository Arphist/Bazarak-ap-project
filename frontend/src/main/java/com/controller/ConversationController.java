package com.controller;

import com.model.Conversation;
import com.model.Message;
import com.model.User;
import com.service.ConversationService;
import com.util.NavigationUtil;
import com.util.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

import java.util.List;

public class ConversationController {

    // ============================================
    // FXML FIELDS
    // ============================================

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

    // ============================================
    // DATA
    // ============================================

    private ObservableList<Conversation> conversations = FXCollections.observableArrayList();
    private ObservableList<Message> messages = FXCollections.observableArrayList();
    private Conversation currentConversation;

}
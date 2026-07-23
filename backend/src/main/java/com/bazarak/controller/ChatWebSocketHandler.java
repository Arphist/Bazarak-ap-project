package com.bazarak.controller;

import com.bazarak.dto.ChatMessage;
import com.bazarak.entity.Message;
import com.bazarak.entity.User;
import com.bazarak.service.ConversationService;
import com.bazarak.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ConcurrentHashMap<Long, ConcurrentHashMap<String, WebSocketSession>> conversationSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConversationService conversationService;
    private final UserService userService;

    public ChatWebSocketHandler(ConversationService conversationService, UserService userService) {
        this.conversationService = conversationService;
        this.userService = userService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // Extract conversationId from query parameter
        String query = session.getUri().getQuery();
        Long conversationId = extractConversationId(query);
        if (conversationId != null) {
            conversationSessions.computeIfAbsent(conversationId, k -> new ConcurrentHashMap<>())
                    .put(session.getId(), session);
            System.out.println("✅ WebSocket connected to conversation: " + conversationId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Parse incoming message
        ChatMessage chatMessage = objectMapper.readValue(message.getPayload(), ChatMessage.class);
        Long conversationId = Long.parseLong(chatMessage.getConversationId());

        // Get sender
        User sender = userService.getUserById(Long.parseLong(chatMessage.getSenderId()));
        userService.isUserBanned(sender);

        // Validate participant
        conversationService.validateParticipant(conversationId, sender);

        // Save message to database
        Message savedMessage = conversationService.sendMessage(
                conversationId,
                sender,
                chatMessage.getContent()
        );

        // Update message with saved data
        chatMessage.setId(savedMessage.getId().toString());
        chatMessage.setSenderUsername(sender.getUsername());
        chatMessage.setTimestamp(savedMessage.getSentAt());

        // Broadcast to all sessions in this conversation EXCEPT sender
        String messageJson = objectMapper.writeValueAsString(chatMessage);
        ConcurrentHashMap<String, WebSocketSession> sessions = conversationSessions.get(conversationId);
        if (sessions != null) {
            for (WebSocketSession s : sessions.values()) {
                if (!s.getId().equals(session.getId()) && s.isOpen()) {
                    s.sendMessage(new TextMessage(messageJson));
                }
            }
        }

        // Also send back to sender to confirm (optional)
        session.sendMessage(new TextMessage(messageJson));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // Remove session from all conversations
        for (ConcurrentHashMap<String, WebSocketSession> sessions : conversationSessions.values()) {
            sessions.remove(session.getId());
        }
        System.out.println("❌ WebSocket disconnected: " + session.getId());
    }

    private Long extractConversationId(String query) {
        if (query == null) return null;
        String[] parts = query.split("&");
        for (String part : parts) {
            if (part.startsWith("conversationId=")) {
                return Long.parseLong(part.split("=")[1]);
            }
        }
        return null;
    }
}
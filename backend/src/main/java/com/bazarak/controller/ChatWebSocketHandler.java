package com.bazarak.controller;

import com.bazarak.dto.ChatMessage;
import com.bazarak.entity.Message;
import com.bazarak.entity.User;
import com.bazarak.service.ConversationService;
import com.bazarak.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


import java.util.concurrent.ConcurrentHashMap;

public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ConcurrentHashMap<Long, ConcurrentHashMap<String, WebSocketSession>> conversationSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

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
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            ChatMessage chatMessage = objectMapper.readValue(message.getPayload(), ChatMessage.class);
            Long conversationId = Long.parseLong(chatMessage.getConversationId());

            User sender = userService.getUserById(Long.parseLong(chatMessage.getSenderId()));
            userService.isUserBanned(sender);
            conversationService.validateParticipant(conversationId, sender);

            Message savedMessage = conversationService.sendMessage(
                    conversationId,
                    sender,
                    chatMessage.getContent()
            );

            chatMessage.setId(savedMessage.getId().toString());
            chatMessage.setSenderUsername(sender.getUsername());
            chatMessage.setTimestamp(savedMessage.getSentAt());

            String messageJson = objectMapper.writeValueAsString(chatMessage);
            ConcurrentHashMap<String, WebSocketSession> sessions = conversationSessions.get(conversationId);
            if (sessions != null) {
                for (WebSocketSession s : sessions.values()) {
                    if (!s.getId().equals(session.getId()) && s.isOpen()) {
                        s.sendMessage(new TextMessage(messageJson));
                    }
                }
            }

            session.sendMessage(new TextMessage(messageJson));

        } catch (Exception e) {
            System.err.println("⚠️ Error handling message: " + e.getMessage());
            e.printStackTrace();
            try {
                session.sendMessage(new TextMessage("{\"error\":\"" + e.getMessage() + "\"}"));
            } catch (Exception sendError) {
                System.err.println("Failed to send error message: " + sendError.getMessage());
            }
        }
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
        try {
            String[] parts = query.split("&");
            for (String part : parts) {
                if (part.startsWith("conversationId=")) {
                    return Long.parseLong(part.split("=")[1]);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse conversationId: " + e.getMessage());
        }
        return null;
    }
}
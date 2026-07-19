package com.bazarak.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for WebSocket messages
 * This is LIGHTWEIGHT compared to the Message entity
 */
public class ChatMessage {
    private String id;
    private String conversationId;
    private String senderId;
    private String senderUsername;
    private String content;
    private LocalDateTime timestamp;

    // Constructors
    public ChatMessage(){}

    public ChatMessage (String conversationId, String senderId, String senderUsername, String content){
        this.content=content;
        this.conversationId=conversationId;
        this.senderId=senderId;
        this.senderUsername=senderUsername;
        this.timestamp=LocalDateTime.now();
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

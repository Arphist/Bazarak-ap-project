package com.bazarak.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for WebSocket messages
 * This is LIGHTWEIGHT compared to the Message entity
 */
public class ChatMessage {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderUsername;
    private String content;
    private LocalDateTime timestamp;

    // Constructors
    public ChatMessage(){}

    public ChatMessage (Long conversationId, Long senderId, String senderUsername, String content){
        this.content=content;
        this.conversationId=conversationId;
        this.senderId=senderId;
        this.senderUsername=senderUsername;
        this.timestamp=LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
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
}

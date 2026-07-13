package com.bazarak.controller;

import com.bazarak.dto.ChatMessage;
import com.bazarak.entity.Message;
import com.bazarak.exception.user.UserNotFoundException;
import com.bazarak.service.ConversationService;
import com.bazarak.service.UserService;
import com.bazarak.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;

@Controller
public class WebSocketChatController {

    @Autowired
    private ConversationService conversationService;
    @Autowired
    private UserService userService;

    /**
     * Handle incoming chat messages
     * Client sends to: /app/chat
     * Server broadcasts to: /topic/messages
     */
    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public ChatMessage handleChat(ChatMessage message) {
        // 1. Sanitize content (prevent XSS)
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setTimestamp(LocalDateTime.now());

        // 2. Save to database
        try {
            Long conversationId = Long.parseLong(String.valueOf(message.getConversationId()));
            User sender = userService.getUserById(message.getSenderId());
            conversationService.validateParticipant(conversationId,sender);
            Message savedMessage = conversationService.sendMessageAndBroadcast(
                    conversationId,
                    sender,
                    message.getContent()
            );

            // Update message with saved data
            message.setId(savedMessage.getId());
            message.setSenderUsername(savedMessage.getSender().getUsername());
            message.setTimestamp(savedMessage.getSentAt());
        } catch (Exception e) {
            System.err.println("Error saving message: " + e.getMessage());
        }

        // 3. Broadcast to all subscribers
        return message;
    }
}
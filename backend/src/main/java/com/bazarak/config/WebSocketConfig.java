package com.bazarak.config;

import com.bazarak.controller.ChatWebSocketHandler;
import com.bazarak.service.ConversationService;
import com.bazarak.service.UserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ConversationService conversationService;
    private final UserService userService;

    public WebSocketConfig(ConversationService conversationService, UserService userService) {
        this.conversationService = conversationService;
        this.userService = userService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new ChatWebSocketHandler(conversationService, userService), "/ws")
                .setAllowedOrigins("*");
    }
}
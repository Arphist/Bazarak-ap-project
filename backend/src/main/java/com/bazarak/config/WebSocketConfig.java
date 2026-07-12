package com.bazarak.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer{

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config){
        // Enable in-memory message broker for broadcasting
        config.enableSimpleBroker("/topic");
        // Client messages should be prefixed with /app
        config.setApplicationDestinationPrefixes("/app");
    }
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket endpoint: ws://localhost:8080/ws
        // This means: "Clients can connect to ws://your-server.com/ws"
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*")
                .withSockJS();  // Fallback for browsers without WebSocket
    }
}

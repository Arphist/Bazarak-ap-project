package com.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.model.ChatMessage;
import com.util.Config;
import com.util.HttpClientUtil;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.util.function.Consumer;

public class ChatWebSocketClient extends WebSocketClient {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private Consumer<ChatMessage> messageHandler;
    private volatile boolean isConnected = false;

    public ChatWebSocketClient(Consumer<ChatMessage> messageHandler, Long conversationId) throws Exception {
        super(new URI(Config.WS_URL + "?conversationId=" + conversationId), new Draft_6455());
        this.messageHandler = messageHandler;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        isConnected = true;
        System.out.println("WebSocket connected!");
    }

    @Override
    public void onMessage(String message) {
        try {
            ChatMessage chatMessage = objectMapper.readValue(message, ChatMessage.class);
            if (messageHandler != null) {
                messageHandler.accept(chatMessage);
            }
        } catch (Exception e) {
            System.err.println("Error parsing WebSocket message: " + e.getMessage());
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        isConnected = false;
        System.out.println("WebSocket disconnected: code=" + code + " reason=" + reason + " remote=" + remote);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }

    /**
     * Send a chat message via WebSocket
     */
    public void sendMessage(ChatMessage message) {
        if (!isConnected) {
            System.err.println("Cannot send message: WebSocket not connected");
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(message);
            send(json);
        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return isConnected;
    }
}
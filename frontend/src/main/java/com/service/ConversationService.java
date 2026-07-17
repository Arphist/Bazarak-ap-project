package com.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.type.MapType;
import com.model.Category;
import com.model.Conversation;
import com.model.Message;
import com.util.Config;
import com.util.HttpClientUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConversationService {
    private static final HttpClient httpClient = HttpClientUtil.getHttpClient();
    private static final ObjectMapper objectMapper = HttpClientUtil.getObjectMapper();
    private static final String url = Config.BASE_URL + "/conversations";

    /**
     * Retrieves all conversations from the backend.
     *
     * @return list of all conversations
     * @throws Exception if the request fails
     */
    public static List<Conversation> getAllConversations() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> responseObj = objectMapper.readValue(response.body(), Map.class);
            Object conversationsObj = responseObj.get("conversations");
            if (conversationsObj != null) {
                String conversationJson = objectMapper.writeValueAsString(conversationsObj);
                return objectMapper.readValue(conversationJson, new TypeReference<List<Conversation>>() {
                });
            } else {
                return new ArrayList<>();
            }
        } else {
            Map<String, String> error = objectMapper.readValue(response.body(),Map.class);
            throw new Exception(error.getOrDefault("error","Failed to load conversations"));
        }
    }

    /**
     * Retrieves a conversation by its unique identifier.
     *
     * @param id the ID of the conversation to retrieve
     * @return the requested conversation
     * @throws Exception if the request fails or the conversation cannot be loaded
     */
    public static Conversation getConversation (Long id) throws Exception{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url+"/"+id))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> responseObj = objectMapper.readValue(response.body(), Map.class);
            Object conversationObj = responseObj.get("conversation");
            if (conversationObj != null) {
                String conversationJson = objectMapper.writeValueAsString(conversationObj);
                return objectMapper.readValue(conversationJson, Conversation.class);
            } else {
                return objectMapper.readValue(response.body(), Conversation.class);
            }
        } else {
            Map<String, String> error = objectMapper.readValue(response.body(),Map.class);
            throw new Exception(error.getOrDefault("error","Failed to load the conversation"));
        }
    }

    /**
     * Starts a new conversation between the current user and a seller.
     *
     * @param sellerId the ID of the seller
     * @param adId the ID of the advertisement
     * @return a map containing the conversation ID and server message
     * @throws Exception if the conversation cannot be started
     */
    public static Map<String, Object> startConversation(Long sellerId, Long adId) throws Exception {
        // Build request body
        Map<String, Long> requestBody = new HashMap<>();
        requestBody.put("sellerId", sellerId);
        requestBody.put("adId", adId);
        String json = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "/start"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);
            Map<String, Object> result = new HashMap<>();
            result.put("id", responseBody.get("id"));
            result.put("message", responseBody.getOrDefault("message", "Conversation started successfully"));
            return result;
        } else {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to start conversation"));
        }
    }

    /**
     * Retrieves all messages of a conversation.
     *
     * @param conversationId the ID of the conversation
     * @return list of messages in the conversation
     * @throws Exception if the request fails or messages cannot be loaded
     */
    public static List<Message> getMessages(Long conversationId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "/" + conversationId + "/messages"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);
            Object messagesObj = responseBody.get("messages");
            if (messagesObj != null) {
                String messagesJson = objectMapper.writeValueAsString(messagesObj);
                return objectMapper.readValue(messagesJson, new TypeReference<List<Message>>() {});
            }
            return new ArrayList<>();
        } else {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to load messages"));
        }
    }
}
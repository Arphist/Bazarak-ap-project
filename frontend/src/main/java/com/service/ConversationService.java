package com.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.type.MapType;
import com.model.Category;
import com.model.Conversation;
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
import java.util.List;
import java.util.Map;

public class ConversationService {
    private static final HttpClient httpClient = HttpClientUtil.getHttpClient();
    private static final ObjectMapper objectMapper = HttpClientUtil.getObjectMapper();
    private static final String url = Config.BASE_URL + "/conversations";

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
}

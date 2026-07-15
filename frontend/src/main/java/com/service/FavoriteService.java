package com.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.model.Favorite;
import com.util.Config;
import com.util.HttpClientUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class FavoriteService {
    private final static HttpClient httpClient = HttpClientUtil.getHttpClient();
    private final static ObjectMapper objectMapper = HttpClientUtil.getObjectMapper();

    public static void addToFavorite(Long adId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL + "/favorites/" + adId))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 201) {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to add favorite"));
        }
    }

    public static void removeFavorite(Long adId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL + "/favorites/" + adId))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to remove favorite"));
        }
    }

    public static List<Favorite> getFavorites() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL + "/favorites"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
            Object favoritesObj = result.get("favorites");
            //TODO: use other objects like 'count'
            if (favoritesObj != null) {
                String json = objectMapper.writeValueAsString(favoritesObj);
                return objectMapper.readValue(json, new TypeReference<List<Favorite>>() {
                });
            }
            return List.of();
        } else {
            throw new Exception("Failed to fetch favorites");
        }
    }

    public static boolean isFavorited(Long adId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL + "/favorites/check/" + adId))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Boolean> result = objectMapper.readValue(response.body(), Map.class);
            return result.getOrDefault("favorited", false);
        } else {
            return false;
        }
    }

    //TODO: handle "/count/{adId}" too.
}

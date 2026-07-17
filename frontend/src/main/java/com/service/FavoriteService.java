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

    /**
     * Adds an advertisement to the current user's favorites.
     *
     * @param adId the ID of the advertisement to add
     * @throws Exception if the advertisement cannot be added to favorites
     */
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

    /**
     * Removes an advertisement from the current user's favorites.
     *
     * @param adId the ID of the advertisement to remove
     * @throws Exception if the advertisement cannot be removed from favorites
     */
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

    /**
     * Retrieves all favorite advertisements of the current user.
     *
     * @return list of favorite advertisements
     * @throws Exception if the request fails
     */
    public static List<Favorite> getFavorites() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL + "/favorites"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
            Object favoritesObj = result.get("favorites");
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

    /**
     * Checks whether an advertisement is in the current user's favorites.
     *
     * @param adId the ID of the advertisement
     * @return true if the advertisement is favorited, otherwise false
     * @throws Exception if the request fails
     */
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

    /**
     * Gets the number of users who have favorited a specific advertisement.
     *
     * @param adId the ID of the advertisement
     * @return the number of favorites
     * @throws Exception if the request fails
     */
    public static Long getFavoriteCount (Long adId) throws Exception{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL+"/favorites/count/"+adId))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode()==200){
            Map<String, Object> result = objectMapper.readValue(response.body(),Map.class);
            Object count = result.get("favoriteCount");
            if (count instanceof Number) {
                return ((Number) count).longValue();
            }
        }
        return 0L;
    }
}
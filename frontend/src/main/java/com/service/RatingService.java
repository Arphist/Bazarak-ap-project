package com.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.model.Advertisement;
import com.model.Rating;
import com.model.User;
import com.util.Config;
import com.util.HttpClientUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class RatingService {
    private static final HttpClient httpClient = HttpClientUtil.getHttpClient();
    private static final ObjectMapper objectMapper = HttpClientUtil.getObjectMapper();
    private static final String url = Config.BASE_URL + "/ratings";

    public static List<Rating> getRatingsBySeller(Long id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "/seller/" + id))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Rating>>() {
            });
        } else {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to load ratings"));
        }
    }

    public static Map<String, Object> getAverageScore(Long id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "/seller/" + id + "/average"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
            Map<String, Object> map = new HashMap<>();
            map.put("sellerId", result.get("sellerId"));
            map.put("averageScore", result.get("averageScore"));
            map.put("totalRatings", result.get("totalRatings"));

            return map;
        } else {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to load average rating"));
        }
    }

    public static List<Rating> getMyRatings() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "/my-ratings"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Rating>>() {
            });
        } else {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to load ratings"));
        }
    }

    public static List<Rating> getRatingsByAdvertisement(Long id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "/advertisement/" + id))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Rating>>() {
            });
        } else {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to load ratings"));
        }
    }

    public static Rating createRating(Rating rating) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("score", rating.getScore());
        requestBody.put("comment", rating.getComment() != null ? rating.getComment() : "");
        requestBody.put("sellerId", rating.getSeller().getId());
        requestBody.put("advertisementId", rating.getAdvertisement().getId());
        String json = objectMapper.writeValueAsString(requestBody);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            return objectMapper.readValue(response.body(), Rating.class);
        } else {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to create ratings"));
        }
    }

    public static String deleteRating(Long id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "/" + id))
                .DELETE().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, String> map = new HashMap<>();
            Map<String,String> result = objectMapper.readValue(response.body(), Map.class);
            map.put("message",result.get("message"));
            return map.get("message");
        }else{
            Map<String,String> error = objectMapper.readValue(response.body(),Map.class);
            throw new Exception(error.getOrDefault("error","Failed to delete rating"));
        }
    }
}

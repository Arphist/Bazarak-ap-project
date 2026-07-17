package com.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.model.Advertisement;
import com.util.Config;
import com.util.HttpClientUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdService {

    private static final HttpClient httpClient = HttpClientUtil.getHttpClient();
    private static final ObjectMapper objectMapper = HttpClientUtil.getObjectMapper();

    // Get all active ads
    public static List<Advertisement> getActiveAds() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL + "/ads/active"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Advertisement>>() {});
        } else {
            throw new Exception("Failed to load ads: " + response.statusCode());
        }
    }

    // Get ad by ID
    public static Advertisement getAdById(Long id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL + "/ads/" + id))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Advertisement.class);
        } else {
            throw new Exception("Ad not found");
        }
    }

    // Search ads
    public static List<Advertisement> searchAds(String keyword) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL + "/ads/search?keyword=" + keyword))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
            Object adsObj = result.get("results");
            if (adsObj != null) {
                String json = objectMapper.writeValueAsString(adsObj);
                return objectMapper.readValue(json, new TypeReference<List<Advertisement>>() {});
            }
            return new ArrayList<>();
        } else {
            throw new Exception("Search failed");
        }
    }
    // Create new ad
    public static Map<String,Object> createAd(Advertisement ad) throws Exception {
        String json = objectMapper.writeValueAsString(ad);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL + "/ads"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201) {
            Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
            Long id = ((Number) result.get("id")).longValue();

            // Get the full ad details + the backend-message
            Map<String,Object> map = new HashMap<>();
            map.put("message",result.get("message"));
            map.put("ad",getAdById(id));
            return map;
        } else {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to create ad"));
        }
    }


}
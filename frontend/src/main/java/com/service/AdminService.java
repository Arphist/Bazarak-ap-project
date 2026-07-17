package com.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.model.Advertisement;
import com.model.User;
import com.util.Config;
import com.util.HttpClientUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class AdminService {

    private static final HttpClient httpClient = HttpClientUtil.getHttpClient();
    private static final ObjectMapper objectMapper = HttpClientUtil.getObjectMapper();

    // Get pending ads
    public static List<Advertisement> getPendingAds (String sortBy, String sortOrder) throws Exception{
        // Build query parameters
        StringBuilder query = new StringBuilder();
        query.append("sortBy=").append(sortBy != null ? sortBy : "created_at");
        query.append("&sortOrder=").append(sortOrder != null ? sortOrder : "desc");
        String url = Config.BASE_URL + "/admin/ads/pending?" + query.toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
            Object adsObj = result.get("ads");
            if (adsObj != null) {
                String json = objectMapper.writeValueAsString(adsObj);
                return objectMapper.readValue(json, new TypeReference<List<Advertisement>>() {
                });
            }
            return new ArrayList<>();
        } else {
            throw new Exception("Failed to load ads: " + response.statusCode());
        }
    }

    public static Map<String,Object> approveAd (Advertisement ad)throws Exception{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL+"/admin/ads/"+ad.getId()+"/approve"))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);
            Map<String, Object> result = new HashMap<>();
            result.put("message", responseBody.getOrDefault("message", "Ad updated successfully"));
            Object adObj = responseBody.get("ad");
            if (adObj != null) {
                String adJson = objectMapper.writeValueAsString(adObj);
                result.put("ad", objectMapper.readValue(adJson, Advertisement.class));
            } else {
                result.put("ad", objectMapper.readValue(response.body(), Advertisement.class));
            }
            return result;
        } else {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to approve ad"));
        }
    }

    public static Map<String,Object> rejectAd (Advertisement ad)throws Exception{
        String json = objectMapper.writeValueAsString(ad);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL+"/admin/ads/"+ad.getId()+"/reject"))
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);
            Map<String, Object> result = new HashMap<>();
            result.put("message", responseBody.getOrDefault("message", "Ad updated successfully"));
            result.put("reason",responseBody.get("reason"));
            Object adObj = responseBody.get("ad");
            if (adObj != null) {
                String adJson = objectMapper.writeValueAsString(adObj);
                result.put("ad", objectMapper.readValue(adJson, Advertisement.class));
            } else {
                result.put("ad", objectMapper.readValue(response.body(), Advertisement.class));
            }
            return result;
        } else {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to reject ad"));
        }
    }

    public static void deleteAd (Advertisement ad) throws Exception{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL+"/admin/ads/"+ad.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to delete ad"));
        }
    }

    public static List<Advertisement> getAdsByStatus (String status, String sortBy, String sortOrder) throws Exception{
        // Build query parameters
        StringBuilder query = new StringBuilder();
        query.append("sortBy=").append(sortBy != null ? sortBy : "created_at");
        query.append("&sortOrder=").append(sortOrder != null ? sortOrder : "desc");
        String url = Config.BASE_URL + "/admin/ads/status/"+status+"?" + query.toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
            Object adsObj = result.get("ads");
            if (adsObj != null) {
                String json = objectMapper.writeValueAsString(adsObj);
                return objectMapper.readValue(json, new TypeReference<List<Advertisement>>() {
                });
            }
            return new ArrayList<>();
        } else {
            throw new Exception("Failed to load ads: " + response.statusCode());
        }
    }

    public static Map<String, Object> getDashboardStats() throws Exception{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL+"/admin/ads/dashboard"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);
            Map<String, Object> result = new HashMap<>();
            result.put("totalAds", ((Number) responseBody.getOrDefault("totalAds", 0)).longValue());
            result.put("pendingAds", ((Number) responseBody.getOrDefault("pendingAds", 0)).longValue());
            result.put("activeAds", ((Number) responseBody.getOrDefault("activeAds", 0)).longValue());
            result.put("adsByCategory", responseBody.getOrDefault("adsByCategory", List.of()));
            result.put("adsByCity", responseBody.getOrDefault("adsByCity", List.of()));
            result.put("recentAds", ((Number) responseBody.getOrDefault("recentAds", 0)).intValue());

            return result;
        } else {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to load stats"));
        }
    }
}

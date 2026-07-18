package com.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.model.Advertisement;
import com.model.Rating;
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

public class UserAdvertisementService {
    private static final HttpClient httpClient = HttpClientUtil.getHttpClient();
    private static final ObjectMapper objectMapper = HttpClientUtil.getObjectMapper();
    private static final String url = Config.BASE_URL + "/users/me";

    public static List<Advertisement> getMyAds(String sortBy, String sortOrder) throws Exception {
        String sortByParam = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "created_at";
        String sortOrderParam = (sortOrder != null && !sortOrder.isEmpty()) ? sortOrder : "desc";

        String urlWithQuery = url + "/ads" +
                "?sortBy=" + URLEncoder.encode(sortByParam, StandardCharsets.UTF_8) +
                "&sortOrder=" + URLEncoder.encode(sortOrderParam, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlWithQuery))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> responseObj = objectMapper.readValue(response.body(), Map.class);
            Object result = responseObj.get("ads");
            if (result != null) {
                String json = objectMapper.writeValueAsString(result);
                return objectMapper.readValue(json, new TypeReference<List<Advertisement>>() {
                });
            } else {
                return new ArrayList<>();
            }
        } else {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to load ads"));
        }
    }

    public static List<Advertisement> getMyAdsByStatus(String status, String sortBy, String sortOrder) throws Exception {
        String sortByParam = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "created_at";
        String sortOrderParam = (sortOrder != null && !sortOrder.isEmpty()) ? sortOrder : "desc";

        String urlWithQuery = url + "/ads/status/" + status.toUpperCase() +
                "?sortBy=" + URLEncoder.encode(sortByParam, StandardCharsets.UTF_8) +
                "&sortOrder=" + URLEncoder.encode(sortOrderParam, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlWithQuery))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> responseObj = objectMapper.readValue(response.body(), Map.class);
            Object result = responseObj.get("ads");
            if (result != null) {
                String json = objectMapper.writeValueAsString(result);
                return objectMapper.readValue(json, new TypeReference<List<Advertisement>>() {
                });
            } else {
                return new ArrayList<>();
            }
        } else {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to load ads"));
        }
    }

    public static Map<String, Map<String, Object>> getMyAdsDashboard() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "/ads/dashboard"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);

            Map<String, Map<String, Object>> dashboard = new HashMap<>();

            // Extract each status group
            dashboard.put("total", createStatusGroup(responseBody, "total"));
            dashboard.put("pending", createStatusGroup(responseBody, "pending"));
            dashboard.put("active", createStatusGroup(responseBody, "active"));
            dashboard.put("rejected", createStatusGroup(responseBody, "rejected"));
            dashboard.put("sold", createStatusGroup(responseBody, "sold"));
            dashboard.put("deleted", createStatusGroup(responseBody, "deleted"));

            return dashboard;
        } else {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to load dashboard data"));
        }
    }

    // Helper method to extract status group data
    private static Map<String, Object> createStatusGroup(Map<String, Object> responseBody, String key) {
        Map<String, Object> group = new HashMap<>();
        Object data = responseBody.get(key);
        if (data instanceof Map) {
            Map<String, Object> dataMap = (Map<String, Object>) data;
            group.put("count", dataMap.getOrDefault("count", 0));

            Object adsObj = dataMap.get("ads");
            if (adsObj != null) {
                try {
                    String adsJson = objectMapper.writeValueAsString(adsObj);
                    List<Advertisement> ads = objectMapper.readValue(adsJson, new TypeReference<List<Advertisement>>() {});
                    group.put("ads", ads);
                } catch (Exception e) {
                    group.put("ads", new ArrayList<>());
                }
            } else {
                group.put("ads", new ArrayList<>());
            }
        } else {
            group.put("count", 0);
            group.put("ads", new ArrayList<>());
        }
        return group;
    }

    public static Advertisement getMyAd(Long adId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "/ads/" + adId))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Advertisement.class);
        } else {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to load ad"));
        }
    }

    public static List<Advertisement> getMyRecentAds() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "/ads/recent"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> responseObj = objectMapper.readValue(response.body(), Map.class);
            Object result = responseObj.get("ads");
            if (result != null) {
                String json = objectMapper.writeValueAsString(result);
                return objectMapper.readValue(json, new TypeReference<List<Advertisement>>() {});
            }
            return new ArrayList<>();
        } else {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to load recent ads"));
        }
    }

    public static List<Advertisement> getMyActiveAds(String sortBy, String sortOrder) throws Exception {
        String sortByParam = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "created_at";
        String sortOrderParam = (sortOrder != null && !sortOrder.isEmpty()) ? sortOrder : "desc";

        String urlWithQuery = url + "/ads/active" +
                "?sortBy=" + URLEncoder.encode(sortByParam, StandardCharsets.UTF_8) +
                "&sortOrder=" + URLEncoder.encode(sortOrderParam, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlWithQuery))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> responseObj = objectMapper.readValue(response.body(), Map.class);
            Object result = responseObj.get("ads");
            if (result != null) {
                String json = objectMapper.writeValueAsString(result);
                return objectMapper.readValue(json, new TypeReference<List<Advertisement>>() {});
            }
            return new ArrayList<>();
        } else {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to load active ads"));
        }
    }

    public static List<Advertisement> getMyPendingAds(String sortBy, String sortOrder) throws Exception {
        String sortByParam = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "created_at";
        String sortOrderParam = (sortOrder != null && !sortOrder.isEmpty()) ? sortOrder : "desc";

        String urlWithQuery = url + "/ads/pending" +
                "?sortBy=" + URLEncoder.encode(sortByParam, StandardCharsets.UTF_8) +
                "&sortOrder=" + URLEncoder.encode(sortOrderParam, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlWithQuery))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> responseObj = objectMapper.readValue(response.body(), Map.class);
            Object result = responseObj.get("ads");
            if (result != null) {
                String json = objectMapper.writeValueAsString(result);
                return objectMapper.readValue(json, new TypeReference<List<Advertisement>>() {});
            }
            return new ArrayList<>();
        } else {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to load pending ads"));
        }
    }

    public static List<Advertisement> getMyRejectedAds(String sortBy, String sortOrder) throws Exception {
        String sortByParam = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "created_at";
        String sortOrderParam = (sortOrder != null && !sortOrder.isEmpty()) ? sortOrder : "desc";

        String urlWithQuery = url + "/ads/rejected" +
                "?sortBy=" + URLEncoder.encode(sortByParam, StandardCharsets.UTF_8) +
                "&sortOrder=" + URLEncoder.encode(sortOrderParam, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlWithQuery))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> responseObj = objectMapper.readValue(response.body(), Map.class);
            Object result = responseObj.get("ads");
            if (result != null) {
                String json = objectMapper.writeValueAsString(result);
                return objectMapper.readValue(json, new TypeReference<List<Advertisement>>() {});
            }
            return new ArrayList<>();
        } else {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to load rejected ads"));
        }
    }

    public static List<Advertisement> getMySoldAds(String sortBy, String sortOrder) throws Exception {
        String sortByParam = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "created_at";
        String sortOrderParam = (sortOrder != null && !sortOrder.isEmpty()) ? sortOrder : "desc";

        String urlWithQuery = url + "/ads/sold" +
                "?sortBy=" + URLEncoder.encode(sortByParam, StandardCharsets.UTF_8) +
                "&sortOrder=" + URLEncoder.encode(sortOrderParam, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlWithQuery))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> responseObj = objectMapper.readValue(response.body(), Map.class);
            Object result = responseObj.get("ads");
            if (result != null) {
                String json = objectMapper.writeValueAsString(result);
                return objectMapper.readValue(json, new TypeReference<List<Advertisement>>() {});
            }
            return new ArrayList<>();
        } else {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to load sold ads"));
        }
    }

    public static List<Advertisement> getMyDeletedAds(String sortBy, String sortOrder) throws Exception {
        String sortByParam = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "created_at";
        String sortOrderParam = (sortOrder != null && !sortOrder.isEmpty()) ? sortOrder : "desc";

        String urlWithQuery = url + "/ads/deleted" +
                "?sortBy=" + URLEncoder.encode(sortByParam, StandardCharsets.UTF_8) +
                "&sortOrder=" + URLEncoder.encode(sortOrderParam, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlWithQuery))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> responseObj = objectMapper.readValue(response.body(), Map.class);
            Object result = responseObj.get("ads");
            if (result != null) {
                String json = objectMapper.writeValueAsString(result);
                return objectMapper.readValue(json, new TypeReference<List<Advertisement>>() {});
            }
            return new ArrayList<>();
        } else {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to load deleted ads"));
        }
    }
}

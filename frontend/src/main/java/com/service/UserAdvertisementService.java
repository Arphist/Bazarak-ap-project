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

    /**
     * Retrieves all advertisements created by the current user.
     *
     * @param sortBy the field used for sorting
     * @param sortOrder the sorting order (asc or desc)
     * @return list of the user's advertisements
     * @throws Exception if the request fails
     */
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

    /**
     * Retrieves the current user's advertisements with a specific status.
     *
     * @param status the advertisement status
     * @param sortBy the field used for sorting
     * @param sortOrder the sorting order (asc or desc)
     * @return list of matching advertisements
     * @throws Exception if the request fails
     */
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

    /**
     * Retrieves dashboard information for the current user's advertisements.
     *
     * @return a map containing advertisement statistics grouped by status
     * @throws Exception if the request fails
     */
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

    /**
     * Creates a status group from the dashboard response.
     *
     * @param responseBody the dashboard response received from the server
     * @param key the status key to extract
     * @return a map containing the advertisement count and list for the specified status
     */
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

    /**
     * Retrieves one of the current user's advertisements by its ID.
     *
     * @param adId the advertisement ID
     * @return the requested advertisement
     * @throws Exception if the request fails
     */
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
    /**
     * Retrieves the advertisements created by the current user within the last seven days.
     *
     * @return list of recent advertisements
     * @throws Exception if the request fails
     */
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

    /**
     * Retrieves the advertisements marked as sold by the current user.
     *
     * @return list of sold advertisements
     * @throws Exception if the request fails
     */
    public static List<Advertisement> getMySoldAds() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "/ads/sold"))
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

    /**
     * Retrieves the advertisements rejected by the administrator.
     *
     * @return list of rejected advertisements
     * @throws Exception if the request fails
     */
    public static List<Advertisement> getMyRejectedAds() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "/ads/rejected"))
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

    /**
     * Retrieves the advertisements deleted by the current user.
     *
     * @return list of deleted advertisements
     * @throws Exception if the request fails
     */
    public static List<Advertisement> getMyDeletedAds() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "/ads/deleted"))
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
package com.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.model.Advertisement;
import com.model.User;
import com.util.Config;
import com.util.HttpClientUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class AdminService {

    private static final HttpClient httpClient = HttpClientUtil.getHttpClient();
    private static final ObjectMapper objectMapper = HttpClientUtil.getObjectMapper();
    private static final String adsUrl = Config.BASE_URL + "/admin/ads/";
    private static final String usersUrl = Config.BASE_URL + "/admin/users/";

    // OPERATIONS ON ADS

    /**
     * Retrieves all pending advertisements.
     *
     * @param sortBy the field used for sorting
     * @param sortOrder the sorting order (asc or desc)
     * @return list of pending advertisements
     * @throws Exception if the request fails
     */
    public static List<Advertisement> getPendingAds(String sortBy, String sortOrder) throws Exception {
        // Build query parameters
        StringBuilder query = new StringBuilder();
        query.append("sortBy=").append(sortBy != null ? sortBy : "created_at");
        query.append("&sortOrder=").append(sortOrder != null ? sortOrder : "desc");
        String url = adsUrl + "pending?" + query.toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

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

    /**
     * Restores a deleted advertisement by its ID.
     *
     * @param id the ID of the advertisement to restore
     * @return a map containing the restored ad and a success message
     * @throws Exception if the request fails
     */
    public static Map<String, Object> restoreAd (Long id) throws Exception{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(adsUrl + id + "/restore"))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);
            Map<String, Object> result = new HashMap<>();
            result.put("message", responseBody.getOrDefault("message", "Ad restored successfully"));
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
            throw new Exception(error.getOrDefault("error", "Failed to restore ad"));
        }
    }
    /**
     * Approves a pending advertisement.
     *
     * @param ad the advertisement to approve
     * @return a map containing the updated advertisement and the server message
     * @throws Exception if the operation fails
     */
    public static Map<String, Object> approveAd(Advertisement ad) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(adsUrl + ad.getId() + "/approve"))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
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

    /**
     * Rejects a pending advertisement.
     *
     * @param ad the advertisement to reject
     * @return a map containing the updated advertisement, rejection reason, and server message
     * @throws Exception if the operation fails
     */
    public static Map<String, Object> rejectAd(Advertisement ad) throws Exception {
        String json = objectMapper.writeValueAsString(ad.getRejectionReason());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(adsUrl + ad.getId() + "/reject"))
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);
            Map<String, Object> result = new HashMap<>();
            result.put("message", responseBody.getOrDefault("message", "Ad updated successfully"));
            result.put("reason", responseBody.get("reason"));
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

    /**
     * Deletes an advertisement.
     *
     * @param ad the advertisement to delete
     * @throws Exception if the delete operation fails
     */
    public static void deleteAd(Advertisement ad) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(adsUrl + ad.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to delete ad"));
        }
    }

    /**
     * Retrieves advertisements with the specified status.
     *
     * @param status the advertisement status
     * @param sortBy the field used for sorting
     * @param sortOrder the sorting order (asc or desc)
     * @return list of matching advertisements
     * @throws Exception if the request fails
     */
    public static List<Advertisement> getAdsByStatus(String status, String sortBy, String sortOrder) throws Exception {
        // Build query parameters
        StringBuilder query = new StringBuilder();
        query.append("sortBy=").append(sortBy != null ? sortBy : "created_at");
        query.append("&sortOrder=").append(sortOrder != null ? sortOrder : "desc");
        String url = adsUrl + "status/" + status + "?" + query.toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

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

    /**
     * Retrieves dashboard statistics for the administrator.
     *
     * @return a map containing dashboard statistics
     * @throws Exception if the request fails
     */
    public static Map<String, Object> getDashboardStats() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(adsUrl + "dashboard"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);
            Map<String, Object> result = new HashMap<>();
            result.put("totalAds", ((Number) responseBody.getOrDefault("totalAds", 0)).longValue());
            result.put("pendingAds", ((Number) responseBody.getOrDefault("pendingAds", 0)).longValue());
            result.put("activeAds", ((Number) responseBody.getOrDefault("activeAds", 0)).longValue());
            result.put("adsByCategory", responseBody.getOrDefault("adsByCategory", List.of()));
            result.put("adsByCity", responseBody.getOrDefault("adsByCity", List.of()));
            result.put("recentAds", responseBody.getOrDefault("recentAds", List.of()));

            return result;
        } else {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to load stats"));
        }
    }
    /**
     * Retrieves all advertisements in the system.
     *
     * @param sortBy the field used for sorting
     * @param sortOrder the sorting order (asc or desc)
     * @return list of all advertisements
     * @throws Exception if the request fails
     */
    public static List<Advertisement> getAllAds(String sortBy, String sortOrder) throws Exception {
        // Build query parameters
        StringBuilder query = new StringBuilder();
        query.append("sortBy=").append(sortBy != null ? sortBy : "created_at");
        query.append("&sortOrder=").append(sortOrder != null ? sortOrder : "desc");
        String url = adsUrl + "all?" + query.toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

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

    // OPERATIONS ON USERS

    /**
     * Retrieves all users in the system.
     *
     * @return list of all users
     * @throws Exception if the request fails
     */
    public static List<User> getAllUsers() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(usersUrl + "all-users"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
            Object usersObj = result.get("users");
            if (usersObj != null) {
                String json = objectMapper.writeValueAsString(usersObj);
                return objectMapper.readValue(json, new TypeReference<List<User>>() {
                });
            }
            return new ArrayList<>();
        } else {
            throw new Exception("Failed to load users: " + response.statusCode());
        }
    }

    /**
     * Retrieves detailed information about a user.
     *
     * @param userId the ID of the user
     * @return the requested user
     * @throws Exception if the user cannot be found
     */
    public static User getUserDetails(Long userId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(usersUrl + userId))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<User>() {});
        } else {
            throw new Exception("User not found");
        }
    }

    /**
     * Blocks a user account.
     *
     * @param userId the ID of the user to block
     * @return a map containing the blocked user and the server message
     * @throws Exception if the operation fails
     */
    public static Map<String, Object> blockUser (Long userId) throws Exception{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(usersUrl+userId+"/block"))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200){
            Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);
            Map<String,Object> result = new HashMap<>();
            result.put("message",responseBody.get("message"));
            Object blockedUserObj = responseBody.get("user");
            if (blockedUserObj != null){
                String userJson = objectMapper.writeValueAsString(blockedUserObj);
                result.put("user", objectMapper.readValue(userJson, User.class));
            }else{
                result.put("user", objectMapper.readValue(response.body(),Map.class));
            }
            return result;
        }else {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to block user"));
        }
    }

    /**
     * Unblocks a user account.
     *
     * @param userId the ID of the user to unblock
     * @return a map containing the unblocked user and the server message
     * @throws Exception if the operation fails
     */
    public static Map<String, Object> unblockUser (Long userId) throws Exception{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(usersUrl+userId+"/unblock"))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200){
            Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);
            Map<String,Object> result = new HashMap<>();
            result.put("message",responseBody.get("message"));
            Object unblockedUserObj = responseBody.get("user");
            if (unblockedUserObj != null){
                String userJson = objectMapper.writeValueAsString(unblockedUserObj);
                result.put("user", objectMapper.readValue(userJson, User.class));
            }else{
                result.put("user", objectMapper.readValue(response.body(),Map.class));
            }
            return result;
        }else {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to unblock user"));
        }
    }

    /**
     * Retrieves all blocked users.
     *
     * @return list of blocked users
     * @throws Exception if the request fails
     */
    public static List<User> getBlockedUsers() throws Exception{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(usersUrl+"blocked"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
            Object usersObj = result.get("users");
            if (usersObj != null) {
                String json = objectMapper.writeValueAsString(usersObj);
                return objectMapper.readValue(json, new TypeReference<List<User>>() {
                });
            }
            return new ArrayList<>();
        } else {
            throw new Exception("Failed to load users: " + response.statusCode());
        }
    }

    /**
     * Retrieves all active users.
     *
     * @return list of active users
     * @throws Exception if the request fails
     */
    public static List<User> getActiveUsers() throws Exception{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(usersUrl+"active"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
            Object usersObj = result.get("users");
            if (usersObj != null) {
                String json = objectMapper.writeValueAsString(usersObj);
                return objectMapper.readValue(json, new TypeReference<List<User>>() {
                });
            }
            return new ArrayList<>();
        } else {
            throw new Exception("Failed to load users: " + response.statusCode());
        }
    }

    /**
     * Retrieves specifications for an advertisement (Admin only).
     *
     * @param adId the ID of the advertisement
     * @return a map containing the ad and its specifications
     * @throws Exception if the request fails
     */
    public static Map<String, Object> getAdWithSpecifications(Long adId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL + "/admin/ads/" + adId + "/specifications"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Map.class);
        } else {
            throw new Exception("Failed to load ad specifications: " + response.statusCode());
        }
    }
}
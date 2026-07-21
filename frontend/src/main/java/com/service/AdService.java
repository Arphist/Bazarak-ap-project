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

public class AdService {

    private static final HttpClient httpClient = HttpClientUtil.getHttpClient();
    private static final ObjectMapper objectMapper = HttpClientUtil.getObjectMapper();

    /**
     * Retrieves all active advertisements from the backend.
     *
     * @return list of active advertisements
     * @throws Exception if the request fails
     */
    public static List<Advertisement> getActiveAds() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL + "/ads/active"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Advertisement>>() {
            });
        } else {
            throw new Exception("Failed to load ads: " + response.statusCode());
        }
    }

    /**
     * Retrieves an advertisement by its unique identifier.
     *
     * @param id the ID of the advertisement
     * @return a map containing the advertisement, favorite status, and favorite count
     * @throws Exception if the advertisement cannot be loaded
     */
    public static Map<String,Object> getAdById(Long id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL + "/ads/" + id))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);
            Map<String, Object> map = new HashMap<>();
            map.put("isFavorited", responseBody.getOrDefault("isFavorited", false));
            map.put("favoriteCount", responseBody.get("favoriteCount"));
            map.put("ad", responseBody.get("ad"));
            return map;
        } else {
            throw new Exception("Ad not found");
        }
    }

    /**
     * Searches advertisements using the provided filters and sorting options.
     *
     * @param keyword search keyword
     * @param categoryId category identifier
     * @param cityId city identifier
     * @param minPrice minimum price
     * @param maxPrice maximum price
     * @param sortBy field used for sorting
     * @param sortOrder sorting order (asc or desc)
     * @return list of matching advertisements
     * @throws Exception if the search request fails
     */
    public static List<Advertisement> searchAds(
            String keyword,
            Long categoryId,
            Long cityId,
            Long minPrice,
            Long maxPrice,
            String sortBy,
            String sortOrder
    ) throws Exception {
        // Build query parameters
        StringBuilder query = new StringBuilder();

        if (keyword != null && !keyword.trim().isEmpty()) {
            query.append("keyword=").append(URLEncoder.encode(keyword.trim(), StandardCharsets.UTF_8));
        }

        if (categoryId != null) {
            if (!query.isEmpty()) query.append("&");
            query.append("categoryId=").append(categoryId);
        }

        if (cityId != null) {
            if (!query.isEmpty()) query.append("&");
            query.append("cityId=").append(cityId);
        }

        if (minPrice != null) {
            if (!query.isEmpty()) query.append("&");
            query.append("minPrice=").append(minPrice);
        }

        if (maxPrice != null) {
            if (!query.isEmpty()) query.append("&");
            query.append("maxPrice=").append(maxPrice);
        }

        // Add sorting parameters (with defaults)
        if (!query.isEmpty()) query.append("&");
        query.append("sortBy=").append(sortBy != null ? sortBy : "created_at");
        query.append("&sortOrder=").append(sortOrder != null ? sortOrder : "desc");

        String url = Config.BASE_URL + "/ads/search" + (!query.isEmpty() ? "?" + query : "");
        System.out.println("Search URL: " + url);  // For debugging

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
            Object adsObj = result.get("results");
            if (adsObj != null) {
                String json = objectMapper.writeValueAsString(adsObj);
                return objectMapper.readValue(json, new TypeReference<List<Advertisement>>() {
                });
            }
            return new ArrayList<>();
        } else {
            throw new Exception("Search failed: " + response.statusCode());
        }
    }

    /**
     * Creates a new advertisement with specification values.
     *
     * @param ad the advertisement to create
     * @param specificationValues map of specification ID → value
     * @return a map containing the created advertisement and the server message
     * @throws Exception if the advertisement cannot be created
     */
    public static Map<String, Object> createAd(Advertisement ad, Map<Long, String> specificationValues) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("title", ad.getTitle());
        requestBody.put("description", ad.getDescription());
        requestBody.put("price", ad.getPrice());
        requestBody.put("categoryId", ad.getCategory().getId());
        requestBody.put("cityId", ad.getCity().getId());

        // Add specification values
        if (specificationValues != null && !specificationValues.isEmpty()) {
            requestBody.put("specifications", specificationValues);
        }

        String json = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL + "/ads"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201) {
            Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
            Long id = ((Number) result.get("id")).longValue();

            Map<String, Object> map = new HashMap<>();
            map.put("message", result.get("message"));
            map.put("ad", getAdById(id));
            return map;
        } else {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to create ad"));
        }
    }

    /**
     * Updates an existing advertisement.
     *
     * @param ad the advertisement with updated information
     * @return a map containing the updated advertisement and the server message
     * @throws Exception if the update operation fails
     */
    public static Map<String, Object> updateAd(Advertisement ad) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("title", ad.getTitle());
        requestBody.put("description", ad.getDescription());
        requestBody.put("price", ad.getPrice());
        requestBody.put("categoryId", ad.getCategory().getId());
        requestBody.put("cityId", ad.getCity().getId());
        String json = objectMapper.writeValueAsString(requestBody);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL + "/ads/" + ad.getId()))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
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
            throw new Exception(error.getOrDefault("error", "Update failed"));
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
                .uri(URI.create(Config.BASE_URL+"/ads/"+ad.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to delete ad"));
        }
    }

    /**
     * Marks an advertisement as sold.
     *
     * @param ad the advertisement to mark as sold
     * @return a map containing the updated advertisement and the server message
     * @throws Exception if the operation fails
     */
    public static Map<String, Object> markAsSold(Advertisement ad) throws Exception{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL+"/ads/"+ad.getId()+"/sold"))
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
            throw new Exception(error.getOrDefault("error", "Failed to mark as sold"));
        }
    }

    /**
     * Retrieves all advertisements created by a specific user.
     *
     * @param user the owner of the advertisements
     * @return list of the user's advertisements
     * @throws Exception if the request fails
     */
    public static List<Advertisement> getAdsByUser(User user) throws Exception{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL+"/ads/user/"+user.getId()))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);
            Object adsObj = responseBody.get("ads");
            if (adsObj != null) {
                String adsJson = objectMapper.writeValueAsString(adsObj);
                return objectMapper.readValue(adsJson, new TypeReference<List<Advertisement>>() {});
            }
            return new ArrayList<>();
        } else {
            throw new Exception("Failed to load ads: " + response.statusCode());
        }
    }
}
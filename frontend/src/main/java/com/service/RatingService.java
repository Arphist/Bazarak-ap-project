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

    /**
     * Retrieves all ratings for a specific seller.
     *
     * @param id the seller ID
     * @return list of ratings given to the seller
     * @throws Exception if the request fails
     */
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

    /**
     * Retrieves the average rating information for a seller.
     *
     * @param id the seller ID
     * @return a map containing the seller ID, average score, and total number of ratings
     * @throws Exception if the request fails
     */
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

    /**
     * Retrieves all ratings submitted by the currently logged-in user.
     *
     * @return list of the current user's ratings
     * @throws Exception if the request fails
     */
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

    /**
     * Retrieves all ratings for a specific advertisement.
     *
     * @param id the advertisement ID
     * @return list of ratings for the advertisement
     * @throws Exception if the request fails
     */
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

    /**
     * Creates a new rating for a seller and advertisement.
     *
     * @param rating the rating to create
     * @return the created rating
     * @throws Exception if the rating cannot be created
     */
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

    /**
     * Deletes a rating by its ID.
     *
     * @param id the rating ID
     * @return the success message returned by the server
     * @throws Exception if the delete operation fails
     */
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
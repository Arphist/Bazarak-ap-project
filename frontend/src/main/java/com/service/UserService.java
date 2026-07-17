package com.service;

import com.model.Advertisement;
import com.model.User;
import com.util.Config;
import com.util.HttpClientUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class UserService {
    private final static HttpClient httpClient = HttpClientUtil.getHttpClient();
    private final static ObjectMapper objectMapper = HttpClientUtil.getObjectMapper();

    /**
     * Updates the profile information of the current user.
     *
     * @param user the updated user information
     * @return a map containing the updated user and the server message
     * @throws Exception if the profile update fails
     */
    public static Map<String,Object> updateProfile(User user) throws Exception {
        String json = objectMapper.writeValueAsString(user);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL + "/users/me/profile"))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);
            Map<String, Object> result = new HashMap<>();
            result.put("message", responseBody.getOrDefault("message", "Profile updated successfully"));
            Object userObj = responseBody.get("user");
            if (userObj != null) {
                String adJson = objectMapper.writeValueAsString(userObj);
                result.put("user", objectMapper.readValue(adJson, User.class));
            } else {
                result.put("user", objectMapper.readValue(response.body(), User.class));
            }
            return result;
        } else {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Update failed"));
        }
    }

    /**
     * Changes the password of the current user.
     *
     * @param oldPassword the current password
     * @param newPassword the new password
     * @throws Exception if the password change fails
     */
    public static void changePassword (String oldPassword, String newPassword) throws Exception{
        Map<String, String> passwords = Map.of("oldPassword", oldPassword, "newPassword", newPassword);
        String json = objectMapper.writeValueAsString(passwords);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL + "/users/me/change-password"))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200){
            Map<String, String> error = objectMapper.readValue(response.body(),Map.class);
            throw new Exception(error.getOrDefault("error","Password change failed"));
        }
    }

    //TODO: handle "change-photo" too.
}
package com.service;

import com.model.User;
import com.util.Config;
import com.util.HttpClientUtil;
import com.util.SessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
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
     * Retrieves the profile information of the currently authenticated user.
     * <p>
     * This method makes a GET request to the backend endpoint that returns
     * the current user's profile data. The password is removed from the
     * response for security reasons.
     * </p>
     *
     * @return the current user's profile as a {@code User} object
     * @throws Exception if the request fails, the user is not authenticated,
     *                   or the server returns an error response
     */
    public static User getMyProfile() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL + "/profile"))
                .GET().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), User.class);
        } else {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to load user"));
        }
    }

    /**
     * Updates the profile information of the current user.
     *
     * @param user the updated user information
     * @return a map containing the updated user and the server message
     * @throws Exception if the profile update fails
     */
    public static Map<String, Object> updateProfile(User user) throws Exception {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("fullName", user.getFullName());
        requestMap.put("email", user.getEmail());
        requestMap.put("phoneNumber", user.getPhoneNumber());
        String json = objectMapper.writeValueAsString(requestMap);
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
    public static String changePassword(String oldPassword, String newPassword) throws Exception {
        Map<String, String> passwords = new HashMap<>();
        passwords.put("oldPassword", oldPassword);
        passwords.put("newPassword", newPassword);
        String json = objectMapper.writeValueAsString(passwords);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL + "/users/me/change-password"))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String,String> result = objectMapper.readValue(response.body(), Map.class);
            return result.get("message");
            }else{
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Password change failed"));
        }
    }

    /**
     * Updates the profile photo of the current user.
     * <p>
     * This method reuses the ImageService to handle the multipart upload,
     * eliminating code duplication and ensuring consistent behavior.
     * </p>
     *
     * @param file the image file to upload as profile photo
     * @return a map containing the message and the photo URL
     * @throws Exception if the upload fails
     */
    public static Map<String, Object> updateProfilePhoto(File file) throws Exception {
        // Build the request for profile photo upload
        String boundary = "---------------------------" + System.currentTimeMillis();
        byte[] fileContent = java.nio.file.Files.readAllBytes(file.toPath());

        // Build multipart body
        String CRLF = "\r\n";
        StringBuilder body = new StringBuilder();
        body.append("--").append(boundary).append(CRLF);
        body.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(file.getName()).append("\"").append(CRLF);
        body.append("Content-Type: ").append(java.nio.file.Files.probeContentType(file.toPath())).append(CRLF);
        body.append(CRLF);

        // Combine parts
        byte[] bodyBytes = body.toString().getBytes();
        byte[] filePart = fileContent;
        String footer = CRLF + "--" + boundary + "--" + CRLF;

        byte[] finalBody = new byte[bodyBytes.length + filePart.length + footer.getBytes().length];
        System.arraycopy(bodyBytes, 0, finalBody, 0, bodyBytes.length);
        System.arraycopy(filePart, 0, finalBody, bodyBytes.length, filePart.length);
        System.arraycopy(footer.getBytes(), 0, finalBody, bodyBytes.length + filePart.length, footer.getBytes().length);

        // Send request to profile photo endpoint
        String url = Config.BASE_URL + "/users/me/change-photo";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .PUT(HttpRequest.BodyPublishers.ofByteArray(finalBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);
            Map<String, Object> result = new HashMap<>();
            result.put("message", responseBody.getOrDefault("message", "Profile photo updated successfully"));
            result.put("photoUrl", responseBody.get("photoUrl"));

            // Update local session user
            try {
                User currentUser = SessionManager.getCurrentUser();
                if (currentUser != null && result.get("photoUrl") != null) {
                    currentUser.setProfilePhoto((String) result.get("photoUrl"));
                    SessionManager.setCurrentUser(currentUser);
                }
            } catch (Exception e) {
                System.err.println("Failed to update session user: " + e.getMessage());
            }

            return result;
        } else {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to update profile photo"));
        }
    }
}
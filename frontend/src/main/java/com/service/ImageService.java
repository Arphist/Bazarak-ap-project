package com.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.model.Image;
import com.util.Config;
import com.util.HttpClientUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for handling image operations with the backend.
 * Provides methods to upload, retrieve, delete, and manage images for advertisements.
 */
public class ImageService {

    private static final HttpClient httpClient = HttpClientUtil.getHttpClient();
    private static final ObjectMapper objectMapper = HttpClientUtil.getObjectMapper();
    private static final String baseUrl = Config.BASE_URL + "/images";

    // UPLOAD IMAGE

    /**
     * Uploads an image for a specific advertisement.
     *
     * @param adId      the ID of the advertisement
     * @param file      the image file to upload
     * @param isPrimary whether this image should be set as primary
     * @return the uploaded Image object with metadata
     * @throws Exception if the upload fails
     */
    public static Image uploadImage(Long adId, File file, boolean isPrimary) throws Exception {
        // Read file content
        byte[] fileContent = Files.readAllBytes(file.toPath());
        String boundary = "---------------------------" + System.currentTimeMillis();

        // Build multipart request body
        String CRLF = "\r\n";
        StringBuilder body = new StringBuilder();

        // Add file part
        body.append("--").append(boundary).append(CRLF);
        body.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(file.getName()).append("\"").append(CRLF);
        body.append("Content-Type: ").append(Files.probeContentType(file.toPath())).append(CRLF);
        body.append(CRLF);

        // Convert body to byte array with file content
        byte[] bodyBytes = body.toString().getBytes();
        byte[] filePart = fileContent;
        String footer = CRLF + "--" + boundary + "--" + CRLF;

        // Combine all parts
        byte[] finalBody = new byte[bodyBytes.length + filePart.length + footer.getBytes().length];
        System.arraycopy(bodyBytes, 0, finalBody, 0, bodyBytes.length);
        System.arraycopy(filePart, 0, finalBody, bodyBytes.length, filePart.length);
        System.arraycopy(footer.getBytes(), 0, finalBody, bodyBytes.length + filePart.length, footer.getBytes().length);

        // Build URL with isPrimary parameter
        String url = baseUrl + "/upload/" + adId + "?isPrimary=" + isPrimary;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(finalBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Image.class);
        } else {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to upload image"));
        }
    }

    // GET IMAGES FOR AN AD

    /**
     * Retrieves all images associated with an advertisement.
     *
     * @param adId the ID of the advertisement
     * @return a list of Image objects
     * @throws Exception if the request fails
     */
    public static List<Image> getImagesByAd(Long adId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/ad/" + adId))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> responseObj = objectMapper.readValue(response.body(), Map.class);
            Object imagesObj = responseObj.get("images");
            if (imagesObj != null) {
                String json = objectMapper.writeValueAsString(imagesObj);
                return objectMapper.readValue(json, new TypeReference<List<Image>>() {});
            }
            return new ArrayList<>();
        } else {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to load images"));
        }
    }

    // GET SINGLE IMAGE (as bytes)

    /**
     * Retrieves an image file as a byte array.
     *
     * @param imageId the ID of the image
     * @return the image as byte array
     * @throws Exception if the request fails
     */
    public static byte[] getImageBytes(Long imageId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + imageId))
                .GET()
                .build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() == 200) {
            return response.body();
        } else {
            throw new Exception("Failed to load image: " + response.statusCode());
        }
    }

    /**
     * Gets the URL for an image to be used in UI components.
     *
     * @param imageId the ID of the image
     * @return the full URL of the image
     */
    public static String getImageUrl(Long imageId) {
        return Config.BASE_IMAGE_URL + "/images/" + imageId;
    }

    // DELETE IMAGE

    /**
     * Deletes a specific image by its ID.
     *
     * @param imageId the ID of the image to delete
     * @throws Exception if the deletion fails
     */
    public static void deleteImage(Long imageId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + imageId))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to delete image"));
        }
    }

    // DELETE ALL IMAGES FOR AN AD

    /**
     * Deletes all images associated with an advertisement.
     *
     * @param adId the ID of the advertisement
     * @throws Exception if the deletion fails
     */
    public static void deleteAllImages(Long adId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + adId))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to delete images"));
        }
    }

    // SET PRIMARY IMAGE

    /**
     * Sets an image as the primary image for its advertisement.
     *
     * @param imageId the ID of the image to set as primary
     * @return the updated Image object
     * @throws Exception if the operation fails
     */
    public static Image setPrimaryImage(Long imageId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + imageId + "/primary"))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Image.class);
        } else {
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to set primary image"));
        }
    }
}
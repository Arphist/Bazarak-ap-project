package com.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.model.Category;
import com.util.Config;
import com.util.HttpClientUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class CategoryService {

    private static final HttpClient httpClient = HttpClientUtil.getHttpClient();
    private static final ObjectMapper objectMapper = HttpClientUtil.getObjectMapper();

    /**
     * Fetch all categories from the backend.
     *
     * @return list of all categories
     */
    public static List<Category> getAllCategories() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL + "/categories"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Category>>() {});
        } else {
            throw new Exception("Failed to fetch categories: " + response.statusCode());
        }
    }

    /**
     * Fetch only root categories (categories without a parent).
     *
     * @return list of root categories
     */
    public static List<Category> getRootCategories() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL + "/categories/roots"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Category>>() {});
        } else {
            throw new Exception("Failed to fetch root categories: " + response.statusCode());
        }
    }

    /**
     * Fetch sub-categories of a specific category.
     *
     * @param parentId the ID of the parent category
     * @return list of sub-categories
     */
    public static List<Category> getSubCategories(Long parentId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL + "/categories/" + parentId + "/subcategories"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Category>>() {});
        } else {
            throw new Exception("Failed to fetch sub-categories: " + response.statusCode());
        }
    }

    public static Category getCategoryById(Long id) throws Exception{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL+"/categories/"+id))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<Category>() {});
        } else {
            throw new Exception("Failed to load the category: " + response.statusCode());
        }
    }

    public static List<Category> searchCategories (String keyword) throws Exception{
        String url = Config.BASE_URL + "/categories/search";
        if (keyword != null && !keyword.trim().isEmpty()) {
            url += "?keyword=" + URLEncoder.encode(keyword.trim(), StandardCharsets.UTF_8);
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Category>>() {});
        } else {
            throw new Exception("Failed to load the categories: " + response.statusCode());
        }
    }

    public static Category createCategory (Category category) throws Exception{
        String json = objectMapper.writeValueAsString(category);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL+"/categories"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());

        if (response.statusCode()==200||response.statusCode()==201){
            return objectMapper.readValue(response.body(), new TypeReference<Category>(){});
        }else{
            Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to create category"));
        }
    }

    public static Category updateCategory (Long id, Category category) throws Exception{
        String json = objectMapper.writeValueAsString(category);
        HttpRequest request=HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL+"/categories/"+id))
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());
        if (response.statusCode()==200){
            return objectMapper.readValue(response.body(), new TypeReference<Category>() {});
        }else{
            Map<String,String> error = objectMapper.readValue(response.body(),Map.class);
            throw new Exception(error.getOrDefault("error","Failed to update category"));
        }
    }

    public static String deleteCategory (Long id) throws Exception{
        HttpRequest request=HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL+"/categories/"+id))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());
        if (response.statusCode()==200){
            return objectMapper.readValue(response.body(),Map.class).get("message").toString();
        }else{
            Map<String,String> error = objectMapper.readValue(response.body(),Map.class);
            throw new Exception(error.getOrDefault("error","Failed to delete category"));
        }
    }
}
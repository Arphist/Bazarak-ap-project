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
import java.util.HashMap;
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

    /**
     * Retrieves a category by its unique identifier.
     *
     * @param id the ID of the category to retrieve
     * @return the requested category
     * @throws Exception if the request fails or the category cannot be loaded
     */
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

    // TODO: add this to category panel (a search box)
    /**
     * Searches categories using the specified keyword.
     *
     * @param keyword the keyword used to search categories
     * @return a list of matching categories
     * @throws Exception if the request fails
     */
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

    /**
     * Creates a new category in the system.
     *
     * @param category the category to create
     * @return the created category
     * @throws Exception if the category cannot be created
     */
    public static Category createCategory (Category category) throws Exception{
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", category.getName());
        requestBody.put("description", category.getDescription());
        requestBody.put("parentId", category.getParentId());
        String json = objectMapper.writeValueAsString(requestBody);
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

    /**
     * Updates an existing category.
     *
     * @param id the ID of the category to update
     * @param category the updated category information
     * @return the updated category
     * @throws Exception if the update operation fails
     */
    public static Category updateCategory (Long id, Category category) throws Exception{
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", category.getName());
        requestBody.put("description", category.getDescription());
        requestBody.put("parentId", category.getParentId());
        String json = objectMapper.writeValueAsString(requestBody);
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

    /**
     * Deletes a category by its unique identifier.
     *
     * @param id the ID of the category to delete
     * @return the success message returned by the server
     * @throws Exception if the delete operation fails
     */
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
package com.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.model.Category;
import com.util.Config;
import com.util.HttpClientUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

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
}
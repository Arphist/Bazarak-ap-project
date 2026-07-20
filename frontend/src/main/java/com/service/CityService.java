package com.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.model.City;
import com.util.Config;
import com.util.HttpClientUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CityService {

    private static final HttpClient httpClient = HttpClientUtil.getHttpClient();
    private static final ObjectMapper objectMapper = HttpClientUtil.getObjectMapper();

    /**
     * Fetch all cities from the backend.
     *
     * @return list of all cities
     * @throws Exception if the request fails
     */
    public static List<City> getAllCities() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL + "/cities"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
            Object cities = result.get("cities");
            if (cities != null) {
                String citiesJson = objectMapper.writeValueAsString(cities);
                return objectMapper.readValue(citiesJson, new TypeReference<List<City>>() {
                });
            } else {
                return new ArrayList<>();
            }
        } else {
            throw new Exception("Failed to fetch cities: " + response.statusCode());
        }
    }

    /**
     * Retrieves a city by its unique identifier.
     *
     * @param id the ID of the city to retrieve
     * @return the requested city
     * @throws Exception if the request fails or the city cannot be loaded
     */
    public static City getCityById(Long id) throws Exception{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL+ "/cities/"+id))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());

        if (response.statusCode()==200){
            return objectMapper.readValue(response.body(), City.class);
        }else{
            Map<String,String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error","Failed to load the city"));
        }
    }

    /**
     * Search cities by keyword.
     *
     * @param keyword the search keyword
     * @return list of matching cities
     * @throws Exception if the request fails
     */
    public static List<City> searchCities(String keyword) throws Exception {
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL + "/cities/search?keyword=" + encodedKeyword))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> responseObj = objectMapper.readValue(response.body(), Map.class);
            Object cities = responseObj.get("cities");
            if (cities != null) {
                String citiesJson = objectMapper.writeValueAsString(cities);
                return objectMapper.readValue(citiesJson, new TypeReference<List<City>>() {
                });
            }
            return new ArrayList<>();
        } else {
            throw new Exception("Failed to fetch cities by province: " + response.statusCode());
        }
    }

    // TODO: add a list where user can select a province and then they get the cities of the province
    /**
     * Fetch cities by province.
     *
     * @param province the name of the province
     * @return list of cities in that province
     * @throws Exception if the request fails
     */
    public static List<City> getCitiesByProvince(String province) throws Exception {
        String encodedProvince = URLEncoder.encode(province, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL + "/cities/province/" + encodedProvince))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> responseObj = objectMapper.readValue(response.body(), Map.class);
            Object cities = responseObj.get("cities");
            if (cities != null) {
                String citiesJson = objectMapper.writeValueAsString(cities);
                return objectMapper.readValue(citiesJson, new TypeReference<List<City>>() {
                });
            }
            return new ArrayList<>();
        } else {
            throw new Exception("Failed to fetch cities by province: " + response.statusCode());
        }
    }

    /**
     * Creates a new city in the system.
     *
     * @param city the city to create
     * @return the created city
     * @throws Exception if the city cannot be created
     */
    public static City createCity (City city) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name",city.getName());
        requestBody.put("province",city.getProvince());
        String json = objectMapper.writeValueAsString(requestBody);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL+"/cities"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            return objectMapper.readValue(response.body(),City.class);
        }else{
            Map<String,String> error = objectMapper.readValue(response.body(),Map.class);
            throw new Exception(error.getOrDefault("error","Failed to create the city"));
        }
    }

    /**
     * Updates an existing city.
     *
     * @param id the ID of the city to update
     * @param city the updated city information
     * @return the updated city
     * @throws Exception if the update operation fails
     */
    public static City updateCity (Long id, City city) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name",city.getName());
        requestBody.put("province",city.getProvince());
        String json = objectMapper.writeValueAsString(requestBody);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL+"/cities/"+id))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(),City.class);
        }else{
            Map<String,String> error = objectMapper.readValue(response.body(),Map.class);
            throw new Exception(error.getOrDefault("error","Failed to update the city"));
        }
    }

    /**
     * Deletes a city by its unique identifier.
     *
     * @param id the ID of the city to delete
     * @return the success message returned by the server
     * @throws Exception if the delete operation fails
     */
    public static String deleteCity (Long id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL+"/cities/"+id))
                .DELETE()
                .build();
        HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String,String> result =objectMapper.readValue(response.body(),Map.class);
            return result.get("message");
        }else{
            Map<String,String> error = objectMapper.readValue(response.body(),Map.class);
            throw new Exception(error.getOrDefault("error","Failed to delete the city"));
        }
    }
}
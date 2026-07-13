package com.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.model.City;
import com.util.Config;
import com.util.HttpClientUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class CityService {

    private static final HttpClient httpClient = HttpClientUtil.getHttpClient();
    private static final ObjectMapper objectMapper = HttpClientUtil.getObjectMapper();

    /**
     * Fetch all cities from the backend.
     *
     * @return list of all cities
     */
    public static List<City> getAllCities() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL + "/cities"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<City>>() {});
        } else {
            throw new Exception("Failed to fetch cities: " + response.statusCode());
        }
    }

    /**
     * Fetch cities by province.
     *
     * @param province the name of the province
     * @return list of cities in that province
     */
    public static List<City> getCitiesByProvince(String province) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL + "/cities/province/" + province))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<City>>() {});
        } else {
            throw new Exception("Failed to fetch cities by province: " + response.statusCode());
        }
    }

    /**
     * Search cities by keyword.
     *
     * @param keyword the search keyword
     * @return list of matching cities
     */
    public static List<City> searchCities(String keyword) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL + "/cities/search?keyword=" + keyword))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<City>>() {});
        } else {
            throw new Exception("Failed to search cities: " + response.statusCode());
        }
    }
}
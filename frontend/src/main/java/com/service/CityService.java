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
import java.util.List;
import java.util.Map;

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
            Map<String,Object> result= objectMapper.readValue(response.body(), Map.class);
            Object cities = result.get("cities");
            if (cities!=null){
                String citiesJson = objectMapper.writeValueAsString(cities);
                return objectMapper.readValue(citiesJson, new TypeReference<List<City>>() {});
            }else{
                return new ArrayList<>();
            }
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
        String encodedProvince = URLEncoder.encode(province, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL + "/cities/province/" + encodedProvince))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String,Object> responseObj = objectMapper.readValue(response.body(), Map.class);
            Object cities = responseObj.get("cities");
            if (cities!=null){
                String citiesJson = objectMapper.writeValueAsString(cities);
                return objectMapper.readValue(citiesJson, new TypeReference<List<City>>() {});
            }
            return new ArrayList<>();
        } else {
            throw new Exception("Failed to fetch cities by province: " + response.statusCode());
        }
    }
}
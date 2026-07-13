package com.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.model.Advertisement;
import com.util.Config;
import com.util.HttpClientUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdService {

    private static final HttpClient httpClient = HttpClientUtil.getHttpClient();
    private static final ObjectMapper objectMapper = HttpClientUtil.getObjectMapper();

    // Get all active ads
    public static List<Advertisement> getActiveAds() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL + "/ads/active"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Advertisement>>() {});
        } else {
            throw new Exception("Failed to load ads: " + response.statusCode());
        }
    }

    // Get ad by ID
    public static Advertisement getAdById(Long id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.BASE_URL + "/ads/" + id))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Advertisement.class);
        } else {
            throw new Exception("Ad not found");
        }
    }

}
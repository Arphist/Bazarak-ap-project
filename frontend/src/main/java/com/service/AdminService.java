package com.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.model.Advertisement;
import com.model.User;
import com.util.Config;
import com.util.HttpClientUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class AdminService {

    private static final HttpClient httpClient = HttpClientUtil.getHttpClient();
    private static final ObjectMapper objectMapper = HttpClientUtil.getObjectMapper();

    // Get pending ads
    public static List<Advertisement> getPendingAds (String sortBy, String sortOrder) throws Exception{
        // Build query parameters
        StringBuilder query = new StringBuilder();
        query.append("sortBy=").append(sortBy != null ? sortBy : "created_at");
        query.append("&sortOrder=").append(sortOrder != null ? sortOrder : "desc");
        String url = Config.BASE_URL + "/admin/ads/pending?" + query.toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
            Object adsObj = result.get("ads");
            if (adsObj != null) {
                String json = objectMapper.writeValueAsString(adsObj);
                return objectMapper.readValue(json, new TypeReference<List<Advertisement>>() {
                });
            }
            return new ArrayList<>();
        } else {
            throw new Exception("Failed to load ads: " + response.statusCode());
        }
    }
}

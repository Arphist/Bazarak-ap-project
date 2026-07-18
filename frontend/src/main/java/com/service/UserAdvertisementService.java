package com.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.model.Advertisement;
import com.model.Rating;
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

public class UserAdvertisementService {
    private static final HttpClient httpClient = HttpClientUtil.getHttpClient();
    private static final ObjectMapper objectMapper = HttpClientUtil.getObjectMapper();
    private static final String url = Config.BASE_URL + "/users/me";

    public static List<Advertisement> getMyAds(String sortBy, String sortOrder) throws Exception {
        String sortByParam = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "created_at";
        String sortOrderParam = (sortOrder != null && !sortOrder.isEmpty()) ? sortOrder : "desc";

        String urlWithQuery = url + "/ads" +
                "?sortBy=" + URLEncoder.encode(sortByParam, StandardCharsets.UTF_8) +
                "&sortOrder=" + URLEncoder.encode(sortOrderParam, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlWithQuery))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> responseObj = objectMapper.readValue(response.body(), Map.class);
            Object result = responseObj.get("ads");
            if (result != null) {
                String json = objectMapper.writeValueAsString(result);
                return objectMapper.readValue(json, new TypeReference<List<Advertisement>>(){});
            }else{
               return new ArrayList<>();
            }
        }else{ Map<String, String> error = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(error.getOrDefault("error", "Failed to load ads"));
        }
    }
}

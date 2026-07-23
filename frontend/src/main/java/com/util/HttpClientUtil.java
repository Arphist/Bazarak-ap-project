package com.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.http.HttpClient;
import java.time.Duration;

public class HttpClientUtil {
    // CookieManager to store session cookies
    private static final CookieManager cookieManager = new CookieManager(
            null,  // Default CookieStore
            CookiePolicy.ACCEPT_ALL  // CookiePolicy goes here!
    );

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .cookieHandler(cookieManager)
            .build();

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static HttpClient getHttpClient() {
        return httpClient;
    }

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public static void clearCookies() {
        cookieManager.getCookieStore().removeAll();
    }
}
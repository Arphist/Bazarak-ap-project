package com.util;

public class Config {
    // this must be like my backend URL
    // For local: "http://localhost:8080/api"
    // For ngrok: "https://abc123.ngrok.io/api"
    public static final String BASE_URL = "http://localhost:8080/api";
    public static final String BASE_IMAGE_URL = "http://localhost:8080/api";
    public static final String WS_URL = "ws://localhost:8080/api/ws";

    // For Serveo (change this when using Serveo):
    // public static final String BASE_URL = "https://your-subdomain.serveo.net/api";
    // public static final String WS_URL = "wss://your-subdomain.serveo.net/ws";
}
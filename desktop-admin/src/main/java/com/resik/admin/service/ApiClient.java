package com.resik.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.resik.admin.config.AppConfig;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class ApiClient {

    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static <T> T get(String path, Class<T> type) {
        return request("GET", path, null, type);
    }

    public static <T> T post(String path, Object body, Class<T> type) {
        try {
            String json = mapper.writeValueAsString(body);
            return request("POST", path, json, type);
        } catch (Exception e) {
            throw new RuntimeException("Request failed: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getList(String path, Class<T> type) {
        return request("GET", path, null, type);
    }

    private static <T> T request(String method, String path, String body, Class<T> type) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(AppConfig.API_BASE_URL + path))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json");

            if (AppConfig.authToken != null) {
                builder.header("Authorization", "Bearer " + AppConfig.authToken);
            }

            switch (method) {
                case "GET" -> builder.GET();
                case "POST" -> builder.POST(HttpRequest.BodyPublishers.ofString(body != null ? body : "{}"));
                case "PUT" -> builder.PUT(HttpRequest.BodyPublishers.ofString(body != null ? body : "{}"));
                case "DELETE" -> builder.DELETE();
            }

            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new RuntimeException("API Error (" + response.statusCode() + "): " + response.body());
            }

            if (type == Void.class || response.body() == null || response.body().isEmpty()) {
                return null;
            }

            return mapper.readValue(response.body(), type);
        } catch (Exception e) {
            throw new RuntimeException("Connection failed: " + e.getMessage(), e);
        }
    }

    public static Map<String, Object> login(String email, String password) {
        try {
            String body = mapper.writeValueAsString(Map.of("email", email, "password", password));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(AppConfig.API_BASE_URL + "/auth/login"))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new RuntimeException("Login gagal. Periksa email & password.");
            }
            return mapper.readValue(response.body(), Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Login error: " + e.getMessage(), e);
        }
    }
}

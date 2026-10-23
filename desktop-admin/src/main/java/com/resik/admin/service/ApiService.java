package com.resik.admin.service;

import com.resik.admin.config.AppConfig;
import com.resik.admin.model.Sampah;
import com.resik.admin.model.Penjemputan;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

/**
 * ApiService — fetch data from VPS backend instead of DummyData
 */
public class ApiService {

    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // ===== SAMPAH (CRUD to VPS) =====
    public static List<Sampah> getSampah() {
        return getList("/sampah", Sampah.class);
    }

    public static Sampah createSampah(Sampah s) {
        try {
            String json = mapper.writeValueAsString(s);
            HttpRequest req = request("POST", "/sampah", json);
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            return mapper.readValue(res.body(), Sampah.class);
        } catch (Exception e) {
            throw new RuntimeException("Create sampah failed: " + e.getMessage());
        }
    }

    public static Sampah updateSampah(Long id, Sampah s) {
        try {
            String json = mapper.writeValueAsString(s);
            HttpRequest req = request("PUT", "/sampah/" + id, json);
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            return mapper.readValue(res.body(), Sampah.class);
        } catch (Exception e) {
            throw new RuntimeException("Update sampah failed: " + e.getMessage());
        }
    }

    public static void deleteSampah(Long id) {
        try {
            HttpRequest req = request("DELETE", "/sampah/" + id, null);
            client.send(req, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException("Delete sampah failed: " + e.getMessage());
        }
    }

    // ===== PENJEMPUTAN =====
    public static List<Penjemputan> getPenjemputan() {
        return getList("/penjemputan", Penjemputan.class);
    }

    public static List<Penjemputan> getPenjemputanByStatus(String status) {
        return getList("/penjemputan/status/" + status, Penjemputan.class);
    }

    public static Penjemputan assignPetugas(Long penjemputanId, Long petugasId) {
        try {
            HttpRequest req = request("PUT", "/penjemputan/" + penjemputanId + "/assign/" + petugasId, null);
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            return mapper.readValue(res.body(), Penjemputan.class);
        } catch (Exception e) {
            throw new RuntimeException("Assign failed: " + e.getMessage());
        }
    }

    // ===== DOMPET =====
    public static List<Map<String, Object>> getAllDompet() {
        return getMapList("/dompet/all");
    }

    public static Map<String, Object> topupDompet(Long userId, double jumlah) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("jumlah", jumlah);
            String json = mapper.writeValueAsString(body);
            HttpRequest req = request("PUT", "/dompet/topup/" + userId, json);
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            return mapper.readValue(res.body(), Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Topup failed: " + e.getMessage());
        }
    }

    // ===== LEADERBOARD =====
    public static List<Map<String, Object>> getLeaderboard() {
        return getMapList("/leaderboard");
    }

    // ===== LOKASI BANK SAMPAH (CRUD) =====
    public static List<Map<String, Object>> getLokasi() {
        return getMapList("/lokasi");
    }

    public static Map<String, Object> createLokasi(String nama, String alamat, String jam, String telepon) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("nama", nama);
            body.put("alamat", alamat);
            body.put("jamOperasional", jam);
            body.put("telepon", telepon);
            body.put("isActive", true);
            String json = mapper.writeValueAsString(body);
            HttpRequest req = request("POST", "/lokasi", json);
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            return mapper.readValue(res.body(), Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Create lokasi failed: " + e.getMessage());
        }
    }

    public static void deleteLokasi(Long id) {
        try {
            HttpRequest req = request("DELETE", "/lokasi/" + id, null);
            client.send(req, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException("Delete lokasi failed: " + e.getMessage());
        }
    }

    // ===== USERS =====
    public static List<Map<String, Object>> getUsers() {
        // Try /auth/users or fallback to leaderboard data
        try {
            return getMapList("/leaderboard");
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // ===== HELPER METHODS =====
    private static HttpRequest.Builder baseRequest(String method, String path, String body) {
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.API_BASE_URL + path))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json");
        if (AppConfig.authToken != null) {
            b.header("Authorization", "Bearer " + AppConfig.authToken);
        }
        return b;
    }

    private static HttpRequest request(String method, String path, String body) {
        HttpRequest.Builder b = baseRequest(method, path, body);
        switch (method) {
            case "GET" -> b.GET();
            case "POST" -> b.POST(HttpRequest.BodyPublishers.ofString(body != null ? body : "{}"));
            case "PUT" -> b.PUT(HttpRequest.BodyPublishers.ofString(body != null ? body : "{}"));
            case "DELETE" -> b.DELETE();
        }
        return b.build();
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> getList(String path, Class<T> type) {
        try {
            HttpRequest req = request("GET", path, null);
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() >= 400) return new ArrayList<>();
            return mapper.readValue(res.body(), mapper.getTypeFactory().constructCollectionType(List.class, type));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> getMapList(String path) {
        try {
            HttpRequest req = request("GET", path, null);
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() >= 400) return new ArrayList<>();
            return mapper.readValue(res.body(), new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}

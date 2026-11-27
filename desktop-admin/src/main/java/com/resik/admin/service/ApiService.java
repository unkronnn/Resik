package com.resik.admin.service;

import com.resik.admin.config.AppConfig;
import com.resik.admin.model.Sampah;
import com.resik.admin.model.Penjemputan;
import com.resik.admin.model.User;
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

    /** Generic JSON POST for admin actions (approval, dll). Throws on HTTP >= 400. */
    public static Map<String, Object> post(String path, Map<String, Object> body) {
        try {
            String json = mapper.writeValueAsString(body == null ? Map.of() : body);
            HttpRequest req = request("POST", path, json);
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() >= 400) {
                throw new RuntimeException("POST " + path + " failed (HTTP " + res.statusCode() + "): " + res.body());
            }
            if (res.body() == null || res.body().isBlank()) return Map.of("ok", true);
            return mapper.readValue(res.body(), new TypeReference<Map<String, Object>>() {});
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("POST " + path + " failed: " + e.getMessage());
        }
    }

    /** Generic JSON PUT. Throws on HTTP >= 400 so callers can show the error
     * instead of silently keeping a local-only change. */
    public static Map<String, Object> put(String path, Map<String, Object> body) {
        try {
            String json = mapper.writeValueAsString(body == null ? Map.of() : body);
            HttpRequest req = request("PUT", path, json);
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() >= 400) {
                throw new RuntimeException("PUT " + path + " failed (HTTP " + res.statusCode() + "): " + res.body());
            }
            if (res.body() == null || res.body().isBlank()) return Map.of("ok", true);
            return mapper.readValue(res.body(), new TypeReference<Map<String, Object>>() {});
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("PUT " + path + " failed: " + e.getMessage());
        }
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
            if (res.statusCode() >= 400) {
                throw new RuntimeException("Assign failed (HTTP " + res.statusCode() + "): " + res.body());
            }
            return mapper.readValue(res.body(), Penjemputan.class);
        } catch (Exception e) {
            throw new RuntimeException("Assign failed: " + e.getMessage());
        }
    }

    /** PUT /penjemputan/{id}/status {"status":"DIPROSES"} — kontrak endpoint baru. */
    public static void updatePenjemputanStatus(Long penjemputanId, String status) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status);
        put("/penjemputan/" + penjemputanId + "/status", body);
    }

    /** PUT /penjemputan/{id}/batal — kontrak endpoint baru. */
    public static void batalkanPenjemputan(Long penjemputanId) {
        put("/penjemputan/" + penjemputanId + "/batal", Map.of());
    }

    /** PUT /penjemputan/{id}/jadwal {"tanggalJemput","waktuJemput"} — kontrak endpoint baru. */
    public static void jadwalkanUlangPenjemputan(Long penjemputanId, String tanggalJemput, String waktuJemput) {
        Map<String, Object> body = new HashMap<>();
        body.put("tanggalJemput", tanggalJemput);
        body.put("waktuJemput", waktuJemput);
        put("/penjemputan/" + penjemputanId + "/jadwal", body);
    }

    /** GET /penjemputan/{id} — detail lengkap (detailList, transaksi, petugas,
     * fotoBuktiUrl). Strict: lempar exception bila endpoint belum ada / gagal,
     * supaya pemanggil bisa jatuh ke data baris yang sudah dimuat. */
    public static Penjemputan getPenjemputanById(Long penjemputanId) {
        try {
            HttpRequest req = request("GET", "/penjemputan/" + penjemputanId, null);
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() >= 400) {
                throw new RuntimeException("GET /penjemputan/" + penjemputanId
                        + " failed (HTTP " + res.statusCode() + "): " + res.body());
            }
            return mapper.readValue(res.body(), Penjemputan.class);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Get penjemputan failed: " + e.getMessage());
        }
    }

    /** PUT /penjemputan/{id}/foto-bukti {"fotoBase64","contentType"} — kontrak
     * endpoint baru. Mengembalikan map berisi "fotoBuktiUrl". */
    public static Map<String, Object> uploadFotoBukti(Long penjemputanId, String fotoBase64, String contentType) {
        Map<String, Object> body = new HashMap<>();
        body.put("fotoBase64", fotoBase64);
        body.put("contentType", contentType);
        return put("/penjemputan/" + penjemputanId + "/foto-bukti", body);
    }

    // ===== VERIFIKASI AKUN =====

    /**
     * Daftar user yang menunggu verifikasi. Coba GET /admin/verifikasi dulu;
     * bila endpoint itu belum ada, pakai GET /user/all dan saring di client
     * berdasarkan field statusVerifikasi == MENUNGGU. Strict: lempar exception
     * bila kedua sumber gagal, supaya UI bisa menampilkan banner "data contoh"
     * alih-alih diam-diam memakai dummy.
     */
    public static List<User> getPendingVerifikasi() {
        try {
            return getListStrict("/admin/verifikasi", User.class);
        } catch (Exception first) {
            List<User> all = getListStrict("/user/all", User.class);
            List<User> pending = new ArrayList<>();
            for (User u : all) {
                if ("MENUNGGU".equalsIgnoreCase(u.getStatusVerifikasi())) pending.add(u);
            }
            return pending;
        }
    }

    /** POST /admin/users/{id}/approve — kontrak endpoint baru. */
    public static void approveUser(Long userId) {
        post("/admin/users/" + userId + "/approve", Map.of());
    }

    /** POST /admin/users/{id}/reject {"alasan":"..."} (alasan opsional) — kontrak baru. */
    public static void rejectUser(Long userId, String alasan) {
        Map<String, Object> body = new HashMap<>();
        if (alasan != null && !alasan.isBlank()) body.put("alasan", alasan);
        post("/admin/users/" + userId + "/reject", body);
    }

    // ===== RATING =====

    /**
     * GET /rating/petugas/{petugasId} -> {"petugasId":..,"rataRata":4.5,"jumlah":12}.
     * Backend mengembalikan rataRata 0.0 / jumlah 0 bila belum ada rating —
     * itu dipetakan ke null di sini supaya pemanggil menampilkan
     * "Belum ada rating" dan TIDAK mengarang angka. Null juga bila endpoint
     * belum ada / gagal / kosong.
     */
    public static Map<String, Object> getRatingPetugas(Long petugasId) {
        try {
            HttpRequest req = request("GET", "/rating/petugas/" + petugasId, null);
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() >= 400 || res.body() == null || res.body().isBlank()) return null;
            Map<String, Object> data = mapper.readValue(res.body(), new TypeReference<Map<String, Object>>() {});
            if (data == null || data.isEmpty() || data.get("rataRata") == null) return null;
            Object jumlah = data.get("jumlah");
            if (jumlah instanceof Number n && n.longValue() == 0) return null;
            return data;
        } catch (Exception e) {
            return null;
        }
    }

    /** POST /rating — body {"penjemputanId":..,"nilai":1-5,"komentar":"..."}
     * (nilai & penjemputanId wajib; komentar opsional; petugas diambil dari
     * penjemputan di sisi server). Belum ada UI pengirim rating di desktop;
     * method ini disediakan agar pemanggil lain tidak mengarang struktur
     * body sendiri. */
    public static Map<String, Object> submitRating(Map<String, Object> body) {
        return post("/rating", body);
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
    /** GET /user/all — daftar semua user (dipakai untuk pemilih petugas dan
     * verifikasi). Mengembalikan list kosong bila API tidak terjangkau. */
    public static List<User> getUsers() {
        return getList("/user/all", User.class);
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

    /** Seperti getList, tapi lempar RuntimeException bila HTTP >= 400 atau
     * koneksi gagal — dipakai saat pemanggil harus membedakan "daftar kosong"
     * dari "API tidak terjangkau". */
    private static <T> List<T> getListStrict(String path, Class<T> type) {
        try {
            HttpRequest req = request("GET", path, null);
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() >= 400) {
                throw new RuntimeException("GET " + path + " failed (HTTP " + res.statusCode() + "): " + res.body());
            }
            return mapper.readValue(res.body(), mapper.getTypeFactory().constructCollectionType(List.class, type));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("GET " + path + " failed: " + e.getMessage());
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

package com.resik.client.api;

import com.resik.client.model.AuthResponse;
import com.resik.client.model.LoginRequest;
import com.resik.client.model.RegisterRequest;
import com.resik.client.model.Sampah;
import com.resik.client.model.Penjemputan;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.PUT;

public interface ResikApi {
    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @GET("sampah")
    Call<List<Sampah>> getAllSampah();

    @GET("sampah/kategori/{kategori}")
    Call<List<Sampah>> getSampahByKategori(@Path("kategori") String kategori);

    @POST("penjemputan")
    Call<Penjemputan> createPenjemputan(@Header("Authorization") String token, @Body Map<String, Object> body);

    @GET("penjemputan/my")
    Call<List<Penjemputan>> getMyPenjemputan(@Header("Authorization") String token);

    @GET("penjemputan")
    Call<List<Penjemputan>> getAllPenjemputan(@Header("Authorization") String token);

    @GET("penjemputan/{id}")
    Call<Map<String, Object>> getPenjemputanDetail(@Header("Authorization") String token, @Path("id") Long id);

    // --- Fitur baru (kontrak backend feat/all-features, Nov 2026).
    // Endpoint-endpoint di bawah ini dibuat mengikuti kontrak yang diberikan
    // agen backend; saat penulisan kode ini sebagian belum ada di
    // backend-api (agen backend mengerjakannya paralel). Bila server
    // menjawab 404 untuk salah satunya, layar menanganinya sebagai
    // "fitur belum tersedia di server" dan tetap berjalan.

    /** PUT penjemputan/{id}/batal — body kosong, status menjadi DIBATALKAN. */
    @PUT("penjemputan/{id}/batal")
    Call<Map<String, Object>> batalkanPenjemputan(@Header("Authorization") String token, @Path("id") Long id);

    /** PUT penjemputan/{id}/jadwal — body {"tanggalJemput","waktuJemput"}. */
    @PUT("penjemputan/{id}/jadwal")
    Call<Map<String, Object>> jadwalkanUlangPenjemputan(@Header("Authorization") String token, @Path("id") Long id, @Body Map<String, Object> body);

    /** POST penjemputan/timbang — body {"penjemputanId","details":[{"detailId","beratAktual"}]}. */
    @POST("penjemputan/timbang")
    Call<Map<String, Object>> timbangSampah(@Header("Authorization") String token, @Body Map<String, Object> body);

    /** PUT penjemputan/{id}/foto-bukti — body {"fotoBase64","contentType"}. */
    @PUT("penjemputan/{id}/foto-bukti")
    Call<Map<String, Object>> uploadFotoBukti(@Header("Authorization") String token, @Path("id") Long id, @Body Map<String, Object> body);

    /** POST rating — body {"penjemputanId","nilai","komentar"}. */
    @POST("rating")
    Call<Map<String, Object>> kirimRating(@Header("Authorization") String token, @Body Map<String, Object> body);

    /** GET rating/petugas/{id} — {"petugasId","rataRata","jumlah"}. */
    @GET("rating/petugas/{id}")
    Call<Map<String, Object>> getRatingPetugas(@Header("Authorization") String token, @Path("id") Long id);

    /** GET rating/penjemputan/{id} — rating, atau 404 bila belum dinilai. */
    @GET("rating/penjemputan/{id}")
    Call<Map<String, Object>> getRatingPenjemputan(@Header("Authorization") String token, @Path("id") Long id);

    /** POST notifikasi/token — body {"token","platform":"android"} (upsert token FCM). */
    @POST("notifikasi/token")
    Call<Map<String, Object>> kirimFcmToken(@Header("Authorization") String token, @Body Map<String, Object> body);

    @GET("user/me")
    Call<Map<String, Object>> getMe(@Header("Authorization") String token);

    // Realtime support: re-fetch endpoints hit when a /ws/events message
    // arrives, so screens update live without a full reload.
    @GET("dompet/saldo")
    Call<Map<String, Object>> getSaldo(@Header("Authorization") String token);

    @GET("dompet/riwayat")
    Call<List<Map<String, Object>>> getRiwayatDompet(@Header("Authorization") String token);

    @GET("leaderboard")
    Call<List<Map<String, Object>>> getLeaderboard();

    @GET("notifikasi")
    Call<List<Map<String, Object>>> getNotifikasi(@Header("Authorization") String token);

    @POST("chat")
    Call<Map<String, Object>> sendChat(@Body Map<String, Object> body);

    @GET("chat/{userId}")
    Call<List<Map<String, Object>>> getChats(@Path("userId") Long userId);
}

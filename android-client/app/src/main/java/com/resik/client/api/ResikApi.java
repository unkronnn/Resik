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

    @GET("user/me")
    Call<Map<String, Object>> getMe(@Header("Authorization") String token);

    @GET("notifikasi")
    Call<List<Map<String, Object>>> getNotifikasi(@Header("Authorization") String token);

    @POST("chat")
    Call<Map<String, Object>> sendChat(@Body Map<String, Object> body);

    @GET("chat/{userId}")
    Call<List<Map<String, Object>>> getChats(@Path("userId") Long userId);
}

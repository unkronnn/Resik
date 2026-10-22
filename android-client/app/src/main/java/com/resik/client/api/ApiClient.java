package com.resik.client.api;

import android.content.Context;
import android.content.SharedPreferences;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    private static final String BASE_URL = "http://10.0.2.2:8080/api/"; // Emulator -> host
    private static ResikApi api;
    private static SharedPreferences prefs;

    public static void init(Context context) {
        prefs = context.getSharedPreferences("resik_prefs", Context.MODE_PRIVATE);

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(ResikApi.class);
    }

    public static ResikApi getApi() {
        return api;
    }

    public static void saveToken(String token) {
        prefs.edit().putString("token", token).apply();
    }

    public static String getToken() {
        return prefs.getString("token", null);
    }

    public static void saveUserId(Long id) {
        prefs.edit().putLong("userId", id).apply();
    }

    public static Long getUserId() {
        return prefs.getLong("userId", -1);
    }

    public static String getAuthHeader() {
        return "Bearer " + getToken();
    }

    public static boolean isLoggedIn() {
        return getToken() != null;
    }

    public static void logout() {
        prefs.edit().clear().apply();
    }
}

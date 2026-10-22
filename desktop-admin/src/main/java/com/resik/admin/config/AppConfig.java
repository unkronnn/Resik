package com.resik.admin.config;

public class AppConfig {
    public static final String API_BASE_URL = System.getenv()
            .getOrDefault("RESIK_API_URL", "http://localhost:8080/api");
    public static String authToken = null;
    public static Long userId = null;
    public static String userRole = null;
    public static String userName = null;
}

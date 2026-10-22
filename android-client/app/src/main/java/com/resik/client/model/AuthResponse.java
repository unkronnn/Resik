package com.resik.client.model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class AuthResponse {
    @SerializedName("token")
    private String token;

    @SerializedName("userId")
    private Long userId;

    @SerializedName("nama")
    private String nama;

    @SerializedName("email")
    private String email;

    @SerializedName("role")
    private String role;

    public String getToken() { return token; }
    public Long getUserId() { return userId; }
    public String getNama() { return nama; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}

package com.resik.client.model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class RegisterRequest {
    @SerializedName("nama")
    private String nama;
    @SerializedName("email")
    private String email;
    @SerializedName("password")
    private String password;
    @SerializedName("alamat")
    private String alamat;
    @SerializedName("noTelp")
    private String noTelp;
    @SerializedName("role")
    private String role;

    public RegisterRequest(String nama, String email, String password, String alamat, String noTelp) {
        this.nama = nama;
        this.email = email;
        this.password = password;
        this.alamat = alamat;
        this.noTelp = noTelp;
        this.role = "MASYARAKAT";
    }
}

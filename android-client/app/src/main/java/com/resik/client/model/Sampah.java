package com.resik.client.model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class Sampah {
    @SerializedName("id")
    private Long id;
    @SerializedName("kategori")
    private String kategori;
    @SerializedName("nama")
    private String nama;
    @SerializedName("hargaPerKg")
    private BigDecimal hargaPerKg;
    @SerializedName("deskripsi")
    private String deskripsi;
    @SerializedName("isActive")
    private Boolean isActive;

    public Long getId() { return id; }
    public String getKategori() { return kategori; }
    public String getNama() { return nama; }
    public BigDecimal getHargaPerKg() { return hargaPerKg; }
    public String getDeskripsi() { return deskripsi; }
    public Boolean getActive() { return isActive; }
}

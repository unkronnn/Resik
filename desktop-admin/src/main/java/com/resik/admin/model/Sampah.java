package com.resik.admin.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Sampah {
    private Long id;
    private String kategori;
    private String nama;
    private BigDecimal hargaPerKg;
    private String deskripsi;
    private Boolean isActive;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public BigDecimal getHargaPerKg() { return hargaPerKg; }
    public void setHargaPerKg(BigDecimal hargaPerKg) { this.hargaPerKg = hargaPerKg; }
    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}

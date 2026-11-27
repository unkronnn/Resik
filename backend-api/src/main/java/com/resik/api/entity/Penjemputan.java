package com.resik.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "penjemputan")
public class Penjemputan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "petugas_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User petugas;

    @Column(name = "tanggal_jemput", nullable = false)
    private LocalDate tanggalJemput;

    @Column(name = "waktu_jemput")
    private String waktuJemput;

    @Column(nullable = false)
    private String alamat;

    @Column(columnDefinition = "TEXT")
    private String catatan;

    @Enumerated(EnumType.STRING)
    private PenjemputanStatus status = PenjemputanStatus.MENUNGGU;

    private BigDecimal latitude;
    private BigDecimal longitude;

    @Column(name = "qr_code", unique = true)
    private String qrCode;

    /**
     * URL penyajian foto bukti timbangan, mis.
     * {@code /api/penjemputan/12/foto}. Diset oleh
     * {@code PUT /api/penjemputan/{id}/foto-bukti}; file-nya disimpan di
     * {@code uploads/bukti/{id}.{jpg|png|webp}} di bawah direktori kerja
     * backend. Dipilih di Penjemputan (bukan Transaksi) karena foto adalah
     * bukti atas satu penjemputan — struk/resit dibangun dari penjemputan
     * dan foto bisa diunggah setelah timbang selesai — dan satu
     * penjemputan punya tepat satu transaksi, jadi tidak ada ambiguitas.
     */
    @Column(name = "foto_bukti_url", length = 500)
    private String fotoBuktiUrl;

    @OneToMany(mappedBy = "penjemputan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetailPenjemputan> detailList = new ArrayList<>();

    @OneToOne(mappedBy = "penjemputan", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Transaksi transaksi;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public User getPetugas() { return petugas; }
    public void setPetugas(User petugas) { this.petugas = petugas; }
    public LocalDate getTanggalJemput() { return tanggalJemput; }
    public void setTanggalJemput(LocalDate tanggalJemput) { this.tanggalJemput = tanggalJemput; }
    public String getWaktuJemput() { return waktuJemput; }
    public void setWaktuJemput(String waktuJemput) { this.waktuJemput = waktuJemput; }
    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }
    public String getCatatan() { return catatan; }
    public void setCatatan(String catatan) { this.catatan = catatan; }
    public PenjemputanStatus getStatus() { return status; }
    public void setStatus(PenjemputanStatus status) { this.status = status; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    public String getFotoBuktiUrl() { return fotoBuktiUrl; }
    public void setFotoBuktiUrl(String fotoBuktiUrl) { this.fotoBuktiUrl = fotoBuktiUrl; }
    public List<DetailPenjemputan> getDetailList() { return detailList; }
    public void setDetailList(List<DetailPenjemputan> detailList) { this.detailList = detailList; }
    public Transaksi getTransaksi() { return transaksi; }
    public void setTransaksi(Transaksi transaksi) { this.transaksi = transaksi; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public enum PenjemputanStatus {
        MENUNGGU, DIJADWALKAN, DIPROSES, SELESAI, DIBATALKAN
    }
}

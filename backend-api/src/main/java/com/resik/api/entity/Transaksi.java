package com.resik.api.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaksi")
public class Transaksi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "penjemputan_id", nullable = false)
    private Penjemputan penjemputan;

    @Column(name = "total_harga", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalHarga;

    @Column(name = "tanggal")
    private LocalDateTime tanggal;

    @Enumerated(EnumType.STRING)
    private TransaksiStatus status = TransaksiStatus.PENDING;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        tanggal = LocalDateTime.now();
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Penjemputan getPenjemputan() { return penjemputan; }
    public void setPenjemputan(Penjemputan penjemputan) { this.penjemputan = penjemputan; }
    public BigDecimal getTotalHarga() { return totalHarga; }
    public void setTotalHarga(BigDecimal totalHarga) { this.totalHarga = totalHarga; }
    public LocalDateTime getTanggal() { return tanggal; }
    public TransaksiStatus getStatus() { return status; }
    public void setStatus(TransaksiStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public enum TransaksiStatus {
        PENDING, BERHASIL, GAGAL
    }
}

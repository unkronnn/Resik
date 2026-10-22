package com.resik.api.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "detail_penjemputan")
public class DetailPenjemputan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "penjemputan_id", nullable = false)
    private Penjemputan penjemputan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sampah_id", nullable = false)
    private Sampah sampah;

    @Column(name = "estimasi_berat", precision = 10, scale = 2)
    private BigDecimal estimasiBerat;

    @Column(name = "berat_aktual", precision = 10, scale = 2)
    private BigDecimal beratAktual;

    @Column(precision = 15, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Penjemputan getPenjemputan() { return penjemputan; }
    public void setPenjemputan(Penjemputan penjemputan) { this.penjemputan = penjemputan; }
    public Sampah getSampah() { return sampah; }
    public void setSampah(Sampah sampah) { this.sampah = sampah; }
    public BigDecimal getEstimasiBerat() { return estimasiBerat; }
    public void setEstimasiBerat(BigDecimal estimasiBerat) { this.estimasiBerat = estimasiBerat; }
    public BigDecimal getBeratAktual() { return beratAktual; }
    public void setBeratAktual(BigDecimal beratAktual) { this.beratAktual = beratAktual; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

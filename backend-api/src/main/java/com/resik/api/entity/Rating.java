package com.resik.api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * One rating of a completed pickup, given by the owning masyarakat to the
 * petugas who carried it out. At most one rating per pickup (enforced by
 * a unique constraint on {@code penjemputan_id} and checked in
 * {@code RatingService}).
 */
@Entity
@Table(name = "rating")
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "penjemputan_id", nullable = false, unique = true)
    private Penjemputan penjemputan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pemberi_rating_id", nullable = false)
    private User pemberiRating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "petugas_id", nullable = false)
    private User petugas;

    /** 1..5 stars. */
    @Column(name = "nilai", nullable = false)
    private Integer nilai;

    @Column(name = "komentar", columnDefinition = "TEXT")
    private String komentar;

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
    public User getPemberiRating() { return pemberiRating; }
    public void setPemberiRating(User pemberiRating) { this.pemberiRating = pemberiRating; }
    public User getPetugas() { return petugas; }
    public void setPetugas(User petugas) { this.petugas = petugas; }
    public Integer getNilai() { return nilai; }
    public void setNilai(Integer nilai) { this.nilai = nilai; }
    public String getKomentar() { return komentar; }
    public void setKomentar(String komentar) { this.komentar = komentar; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

package com.resik.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING)
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nama;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    private String alamat;
    private String noTelp;

    @Column(name = "nip")
    private String nip;
    @Column(name = "shift")
    private String shift;
    @Column(name = "area_tugas")
    private String areaTugas;

    @Column(name = "saldo_dompet", precision = 15, scale = 2)
    private BigDecimal saldoDompet = BigDecimal.ZERO;

    @Column(name = "poin_reward")
    private Integer poinReward = 0;

    /**
     * Account-approval state (admin Verifikasi &amp; Approval flow).
     * New registrations start as {@code MENUNGGU}; an admin moves them to
     * {@code DISETUJUI} or {@code DITOLAK} via {@code /api/admin/users}.
     * Login rejects {@code DITOLAK} accounts; {@code MENUNGGU} accounts can
     * still log in (see AuthService.login) so registration followed by an
     * immediate login keeps working — the flag is visible to clients via
     * {@code AuthResponse.statusVerifikasi} and {@code GET /api/user/me}.
     * Accounts that existed before this field was introduced are
     * grandfathered to {@code DISETUJUI} by migration V3.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status_verifikasi", nullable = false)
    private StatusVerifikasi statusVerifikasi = StatusVerifikasi.MENUNGGU;

    /** Reason supplied by the admin when rejecting an account (nullable). */
    @Column(name = "alasan_penolakan", columnDefinition = "TEXT")
    private String alasanPenolakan;

    private BigDecimal latitude;
    private BigDecimal longitude;

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

    public abstract String getRole();

    public enum StatusVerifikasi {
        MENUNGGU, DISETUJUI, DITOLAK
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }
    public String getNoTelp() { return noTelp; }
    public void setNoTelp(String noTelp) { this.noTelp = noTelp; }
    public String getNip() { return nip; }
    public void setNip(String nip) { this.nip = nip; }
    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }
    public String getAreaTugas() { return areaTugas; }
    public void setAreaTugas(String areaTugas) { this.areaTugas = areaTugas; }
    public BigDecimal getSaldoDompet() { return saldoDompet; }
    public void setSaldoDompet(BigDecimal saldoDompet) { this.saldoDompet = saldoDompet; }
    public Integer getPoinReward() { return poinReward; }
    public void setPoinReward(Integer poinReward) { this.poinReward = poinReward; }
    public StatusVerifikasi getStatusVerifikasi() { return statusVerifikasi; }
    public void setStatusVerifikasi(StatusVerifikasi statusVerifikasi) { this.statusVerifikasi = statusVerifikasi; }
    public String getAlasanPenolakan() { return alasanPenolakan; }
    public void setAlasanPenolakan(String alasanPenolakan) { this.alasanPenolakan = alasanPenolakan; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

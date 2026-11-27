package com.resik.api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * One FCM registration token for one user (one row per device). The token
 * is unique: if the same token is registered again — by the same user on
 * reinstall, or after the device changed owner — the row is updated
 * (upsert) instead of duplicated. Senders look tokens up by user, never
 * by trusting a token supplied by a caller.
 */
@Entity
@Table(name = "device_token")
public class DeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token", nullable = false, unique = true, length = 500)
    private String token;

    /** Client platform as reported by the app, e.g. "android". */
    @Column(name = "platform")
    private String platform;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
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
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

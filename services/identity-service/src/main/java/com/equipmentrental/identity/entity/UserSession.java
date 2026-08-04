package com.equipmentrental.identity.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions")
public class UserSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "refresh_token_hash", nullable = false, unique = true, length = 255)
    private String refreshTokenHash;
    @Column(name = "device_name", length = 150)
    private String deviceName;
    @Column(name = "device_type", length = 50)
    private String deviceType;
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    @Column(name = "login_at", nullable = false, updatable = false)
    private LocalDateTime loginAt;
    @Column(name = "last_activity_at", nullable = false)
    private LocalDateTime lastActivityAt;
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;
    @Column(name = "revoked_reason", length = 255)
    private String revokedReason;

    protected UserSession() {
    }

    public UserSession(User user, String refreshTokenHash, LocalDateTime expiresAt,
                       String deviceName, String deviceType, String ipAddress, String userAgent) {
        this.user = user;
        this.refreshTokenHash = refreshTokenHash;
        this.expiresAt = expiresAt;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        loginAt = now;
        lastActivityAt = now;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getRefreshTokenHash() { return refreshTokenHash; }
    public LocalDateTime getLoginAt() { return loginAt; }
    public LocalDateTime getLastActivityAt() { return lastActivityAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getRevokedAt() { return revokedAt; }
    public String getRevokedReason() { return revokedReason; }
    public String getDeviceName() { return deviceName; }
    public String getDeviceType() { return deviceType; }
    public String getIpAddress() { return ipAddress; }
    public boolean isActive() { return revokedAt == null && expiresAt.isAfter(LocalDateTime.now()); }
    public void touch() { lastActivityAt = LocalDateTime.now(); }
    public void revoke(String reason) { if (revokedAt == null) { revokedAt = LocalDateTime.now(); revokedReason = reason; } }
}

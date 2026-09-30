package com.equipmentrental.identity.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "verification_codes")
public class VerificationCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @Column(nullable = false, length = 150)
    private String email;
    @Column(nullable = false, length = 50)
    private String purpose;
    @Column(name = "code_hash", length = 255)
    private String codeHash;
    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;
    @Column(name = "max_attempts", nullable = false)
    private int maxAttempts = 5;
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    @Column(name = "used_at")
    private LocalDateTime usedAt;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected VerificationCode() {
    }

    public VerificationCode(User user, String email, String purpose, String codeHash, LocalDateTime expiresAt) {
        this.user = user;
        this.email = email;
        this.purpose = purpose;
        this.codeHash = codeHash;
        this.expiresAt = expiresAt;
    }

    @PrePersist
    void prePersist() { createdAt = LocalDateTime.now(); }
    public boolean canUse() { return usedAt == null && attemptCount < maxAttempts && expiresAt.isAfter(LocalDateTime.now()); }
    public void recordAttempt() { attemptCount++; }
    public void markUsed() { usedAt = LocalDateTime.now(); }
    public User getUser() { return user; }
    public String getEmail() { return email; }
    public String getPurpose() { return purpose; }
    public String getCodeHash() { return codeHash; }
}

package com.equipmentrental.identity.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_history")
public class PasswordHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
    @Column(name = "password_hash", nullable = false, length = 255) private String passwordHash;
    @Column(name = "change_reason", nullable = false, length = 50) private String changeReason;
    @Column(name = "changed_at", nullable = false, updatable = false) private LocalDateTime changedAt;
    protected PasswordHistory() { }
    public PasswordHistory(User user, String passwordHash, String changeReason) { this.user = user; this.passwordHash = passwordHash; this.changeReason = changeReason; }
    @PrePersist void prePersist() { changedAt = LocalDateTime.now(); }
}

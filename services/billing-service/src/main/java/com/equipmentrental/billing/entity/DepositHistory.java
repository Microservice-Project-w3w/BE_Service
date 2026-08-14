package com.equipmentrental.billing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "deposit_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "deposit_id", nullable = false)
    private Long depositId;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "old_status", length = 30)
    private String oldStatus;

    @Column(name = "new_status", length = 30)
    private String newStatus;

    @Column(length = 500)
    private String description;

    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
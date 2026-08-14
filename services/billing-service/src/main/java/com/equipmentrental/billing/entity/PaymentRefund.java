package com.equipmentrental.billing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_refunds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRefund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(name = "actor_user_id", nullable = false)
    private Long actorUserId;

    @Column(name = "refunded_at", nullable = false)
    private LocalDateTime refundedAt;

    @PrePersist
    public void prePersist() {
        if (refundedAt == null) {
            refundedAt = LocalDateTime.now();
        }
    }
}
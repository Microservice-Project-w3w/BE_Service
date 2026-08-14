package com.equipmentrental.billing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "invoice_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @Column(nullable = false, length = 50)
    private String action;

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
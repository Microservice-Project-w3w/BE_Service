package com.equipmentrental.billing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "debts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Debt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(
            name = "remaining_amount",
            nullable = false,
            precision = 15,
            scale = 2
    )
    @Builder.Default
    private BigDecimal remainingAmount = BigDecimal.ZERO;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

    @Column(length = 500)
    private String reason;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "OPEN";

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        if (amount == null) {
            amount = BigDecimal.ZERO;
        }

        if (remainingAmount == null
                || remainingAmount.compareTo(BigDecimal.ZERO) == 0) {
            remainingAmount = amount;
        }

        if (status == null) {
            status = "OPEN";
        }
    }
}
package com.equipmentrental.inventory.entity;

import com.equipmentrental.inventory.enums.StockAuditStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_audits")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "organization_id",
            nullable = false
    )
    private Long organizationId;

    @Column(
            name = "branch_id",
            nullable = false
    )
    private Long branchId;

    @Column(
            name = "warehouse_id",
            nullable = false
    )
    private Long warehouseId;

    @Column(
            name = "audit_code",
            nullable = false,
            length = 50
    )
    private String auditCode;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private StockAuditStatus status;

    @Column(length = 500)
    private String note;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "started_by")
    private Long startedBy;

    @Column(name = "completed_by")
    private Long completedBy;

    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        LocalDateTime now =
                LocalDateTime.now();

        if (status == null) {
            status = StockAuditStatus.DRAFT;
        }

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
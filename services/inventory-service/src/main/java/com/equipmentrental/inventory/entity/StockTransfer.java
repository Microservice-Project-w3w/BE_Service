package com.equipmentrental.inventory.entity;

import com.equipmentrental.inventory.enums.StockTransferStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_transfers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "organization_id",
            nullable = false
    )
    private Long organizationId;

    @Column(
            name = "transfer_code",
            nullable = false,
            length = 50
    )
    private String transferCode;

    @Column(
            name = "from_branch_id",
            nullable = false
    )
    private Long fromBranchId;

    @Column(
            name = "from_warehouse_id",
            nullable = false
    )
    private Long sourceWarehouseId;

    @Column(
            name = "to_branch_id",
            nullable = false
    )
    private Long toBranchId;

    @Column(
            name = "to_warehouse_id",
            nullable = false
    )
    private Long destinationWarehouseId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false
    )
    private StockTransferStatus status;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "received_by")
    private Long receivedBy;

    @Column(length = 500)
    private String note;

    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "dispatched_at")
    private LocalDateTime dispatchedAt;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

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
            status = StockTransferStatus.DRAFT;
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
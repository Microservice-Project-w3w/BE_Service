package com.equipmentrental.inventory.entity;

import com.equipmentrental.inventory.enums.StockOutStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_out_receipts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockOutReceipt {

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
            name = "stock_out_code",
            nullable = false,
            length = 50
    )
    private String stockOutCode;

    @Column(
            name = "purpose_type",
            length = 50
    )
    private String purposeType;

    @Column(
            name = "reference_code",
            length = 100
    )
    private String referenceCode;

    @Column(length = 500)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private StockOutStatus status;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "confirmed_by")
    private Long confirmedBy;

    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

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
            status = StockOutStatus.DRAFT;
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

        updatedAt =
                LocalDateTime.now();
    }
}
package com.equipmentrental.inventory.entity;

import com.equipmentrental.inventory.enums.StockAuditResult;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_audit_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAuditItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "stock_audit_id",
            nullable = false
    )
    private Long stockAuditId;

    @Column(
            name = "equipment_id",
            nullable = false
    )
    private Long equipmentId;

    @Column(
            name = "expected_warehouse_id",
            nullable = false
    )
    private Long expectedWarehouseId;

    @Column(name = "expected_location_id")
    private Long expectedLocationId;

    @Column(name = "actual_warehouse_id")
    private Long actualWarehouseId;

    @Column(name = "actual_location_id")
    private Long actualLocationId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "result",
            length = 30
    )
    private StockAuditResult result;

    @Column(length = 500)
    private String note;

    @Column(name = "checked_by")
    private Long checkedBy;

    @Column(name = "checked_at")
    private LocalDateTime checkedAt;

    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
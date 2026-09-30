package com.equipmentrental.inventory.entity;

import com.equipmentrental.inventory.enums.EquipmentTransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "equipment_transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "equipment_id",
            nullable = false
    )
    private Long equipmentId;

    @Column(
            name = "organization_id",
            nullable = false
    )
    private Long organizationId;

    @Column(name = "branch_id")
    private Long branchId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "transaction_type",
            nullable = false,
            length = 30
    )
    private EquipmentTransactionType transactionType;

    @Column(name = "from_warehouse_id")
    private Long fromWarehouseId;

    @Column(name = "to_warehouse_id")
    private Long toWarehouseId;

    @Column(
            name = "reference_type",
            length = 50
    )
    private String referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(
            name = "reference_code",
            length = 100
    )
    private String referenceCode;

    @Column(
            name = "old_status",
            length = 30
    )
    private String oldStatus;

    @Column(
            name = "new_status",
            length = 30
    )
    private String newStatus;

    @Column(name = "performed_by")
    private Long performedBy;

    @Column(length = 500)
    private String note;

    @Column(
            name = "occurred_at",
            nullable = false
    )
    private LocalDateTime occurredAt;

    @PrePersist
    protected void onCreate() {

        if (occurredAt == null) {
            occurredAt = LocalDateTime.now();
        }
    }
}
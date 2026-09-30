package com.equipmentrental.inventory.entity;

import com.equipmentrental.inventory.enums.EquipmentCondition;
import com.equipmentrental.inventory.enums.EquipmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "equipment",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_equipment_org_asset_code",
                        columnNames = {"organization_id", "asset_code"}
                ),
                @UniqueConstraint(
                        name = "uk_equipment_org_serial",
                        columnNames = {"organization_id", "serial_number"}
                ),
                @UniqueConstraint(
                        name = "uk_equipment_org_imei",
                        columnNames = {"organization_id", "imei"}
                ),
                @UniqueConstraint(
                        name = "uk_equipment_org_mac",
                        columnNames = {"organization_id", "mac_address"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "warehouse_location_id")
    private Long warehouseLocationId;

    @Column(name = "model_id", nullable = false)
    private Long modelId;

    @Column(name = "asset_code", nullable = false, length = 100)
    private String assetCode;

    @Column(name = "serial_number", length = 150)
    private String serialNumber;

    @Column(length = 50)
    private String imei;

    @Column(name = "mac_address", length = 50)
    private String macAddress;

    @Column(name = "qr_code", length = 200)
    private String qrCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private EquipmentStatus status = EquipmentStatus.AVAILABLE;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_status", nullable = false, length = 50)
    @Builder.Default
    private EquipmentCondition conditionStatus = EquipmentCondition.GOOD;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "purchase_price", precision = 15, scale = 2)
    private BigDecimal purchasePrice;

    @Column(length = 1000)
    private String note;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }

        if (status == null) {
            status = EquipmentStatus.AVAILABLE;
        }

        if (conditionStatus == null) {
            conditionStatus = EquipmentCondition.GOOD;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
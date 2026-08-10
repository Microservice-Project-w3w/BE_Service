package com.equipmentrental.logistics.entity;

import com.equipmentrental.logistics.entity.enums.ReturnInspectionStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "return_inspections")
public class ReturnInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "return_request_id", nullable = false)
    private Long returnRequestId;

    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    @Column(name = "inspected_by_user_id", nullable = false)
    private Long inspectedByUserId;

    @Column(name = "inspected_at", nullable = false)
    private LocalDateTime inspectedAt;

    @Column(name = "condition_status", nullable = false, length = 50)
    private String conditionStatus;

    @Column(name = "missing_accessories", length = 1000)
    private String missingAccessories;

    @Column(name = "damage_description", length = 2000)
    private String damageDescription;

    @Column(name = "is_damaged", nullable = false)
    private Boolean damaged = false;

    @Column(name = "is_late", nullable = false)
    private Boolean late = false;

    @Column(name = "late_minutes")
    private Long lateMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ReturnInspectionStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (status == null) {
            status = ReturnInspectionStatus.COMPLETED;
        }

        if (damaged == null) {
            damaged = false;
        }

        if (late == null) {
            late = false;
        }
    }

    public Long getId() {
        return id;
    }

    public Long getReturnRequestId() {
        return returnRequestId;
    }

    public void setReturnRequestId(Long returnRequestId) {
        this.returnRequestId = returnRequestId;
    }

    public Long getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
    }

    public Long getInspectedByUserId() {
        return inspectedByUserId;
    }

    public void setInspectedByUserId(Long inspectedByUserId) {
        this.inspectedByUserId = inspectedByUserId;
    }

    public LocalDateTime getInspectedAt() {
        return inspectedAt;
    }

    public void setInspectedAt(LocalDateTime inspectedAt) {
        this.inspectedAt = inspectedAt;
    }

    public String getConditionStatus() {
        return conditionStatus;
    }

    public void setConditionStatus(String conditionStatus) {
        this.conditionStatus = conditionStatus;
    }

    public String getMissingAccessories() {
        return missingAccessories;
    }

    public void setMissingAccessories(String missingAccessories) {
        this.missingAccessories = missingAccessories;
    }

    public String getDamageDescription() {
        return damageDescription;
    }

    public void setDamageDescription(String damageDescription) {
        this.damageDescription = damageDescription;
    }

    public Boolean getDamaged() {
        return damaged;
    }

    public void setDamaged(Boolean damaged) {
        this.damaged = damaged;
    }

    public Boolean getLate() {
        return late;
    }

    public void setLate(Boolean late) {
        this.late = late;
    }

    public Long getLateMinutes() {
        return lateMinutes;
    }

    public void setLateMinutes(Long lateMinutes) {
        this.lateMinutes = lateMinutes;
    }

    public ReturnInspectionStatus getStatus() {
        return status;
    }

    public void setStatus(ReturnInspectionStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

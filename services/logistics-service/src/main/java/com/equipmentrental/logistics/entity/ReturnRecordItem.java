package com.equipmentrental.logistics.entity;

import com.equipmentrental.logistics.entity.enums.ConditionStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "return_record_items")
public class ReturnRecordItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long returnRecordId;

    @Column(nullable = false)
    private Long equipmentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ConditionStatus returnedCondition = ConditionStatus.GOOD;

    @Column(name = "is_damaged", nullable = false)
    private Boolean isDamaged = false;

    @Column(name = "damage_description", length = 1000)
    private String damageDescription;

    @Column(name = "is_missing_accessories", nullable = false)
    private Boolean isMissingAccessories = false;

    @Column(name = "missing_accessories_description", length = 1000)
    private String missingAccessoriesDescription;

    @Column(length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Long getReturnRecordId() {
        return returnRecordId;
    }

    public void setReturnRecordId(Long returnRecordId) {
        this.returnRecordId = returnRecordId;
    }

    public Long getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
    }

    public ConditionStatus getReturnedCondition() {
        return returnedCondition;
    }

    public void setReturnedCondition(ConditionStatus returnedCondition) {
        this.returnedCondition = returnedCondition;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getIsDamaged() {
        return isDamaged;
    }

    public void setIsDamaged(Boolean isDamaged) {
        this.isDamaged = isDamaged;
    }

    public String getDamageDescription() {
        return damageDescription;
    }

    public void setDamageDescription(String damageDescription) {
        this.damageDescription = damageDescription;
    }

    public Boolean getIsMissingAccessories() {
        return isMissingAccessories;
    }

    public void setIsMissingAccessories(Boolean isMissingAccessories) {
        this.isMissingAccessories = isMissingAccessories;
    }

    public String getMissingAccessoriesDescription() {
        return missingAccessoriesDescription;
    }

    public void setMissingAccessoriesDescription(String missingAccessoriesDescription) {
        this.missingAccessoriesDescription = missingAccessoriesDescription;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

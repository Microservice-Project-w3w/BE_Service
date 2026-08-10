package com.equipmentrental.logistics.entity;

import com.equipmentrental.logistics.entity.enums.ReturnRecordStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "return_records")
public class ReturnRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long returnRequestId;

    @Column(nullable = false)
    private Long rentalOrderId;

    @Column(nullable = false)
    private Long inspectorStaffUserId;

    @Column(nullable = false)
    private LocalDateTime actualReturnTime;

    @Column(nullable = false)
    private Boolean isLateReturn = false;

    @Column(name = "late_minutes", nullable = false)
    private Long lateMinutes = 0L;

    @Column(length = 1000)
    private String missingAccessoriesDescription;

    @Column(length = 1000)
    private String conditionDamageDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReturnRecordStatus status = ReturnRecordStatus.DRAFT;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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

    public Long getRentalOrderId() {
        return rentalOrderId;
    }

    public void setRentalOrderId(Long rentalOrderId) {
        this.rentalOrderId = rentalOrderId;
    }

    public Long getInspectorStaffUserId() {
        return inspectorStaffUserId;
    }

    public void setInspectorStaffUserId(Long inspectorStaffUserId) {
        this.inspectorStaffUserId = inspectorStaffUserId;
    }

    public LocalDateTime getActualReturnTime() {
        return actualReturnTime;
    }

    public void setActualReturnTime(LocalDateTime actualReturnTime) {
        this.actualReturnTime = actualReturnTime;
    }

    public Boolean getIsLateReturn() {
        return isLateReturn;
    }

    public void setIsLateReturn(Boolean isLateReturn) {
        this.isLateReturn = isLateReturn;
    }

    public String getMissingAccessoriesDescription() {
        return missingAccessoriesDescription;
    }

    public void setMissingAccessoriesDescription(String missingAccessoriesDescription) {
        this.missingAccessoriesDescription = missingAccessoriesDescription;
    }

    public String getConditionDamageDescription() {
        return conditionDamageDescription;
    }

    public void setConditionDamageDescription(String conditionDamageDescription) {
        this.conditionDamageDescription = conditionDamageDescription;
    }

    public ReturnRecordStatus getStatus() {
        return status;
    }

    public void setStatus(ReturnRecordStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Long getLateMinutes() {
        return lateMinutes;
    }

    public void setLateMinutes(Long lateMinutes) {
        this.lateMinutes = lateMinutes;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

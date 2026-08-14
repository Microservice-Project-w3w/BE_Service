package com.equipmentrental.logistics.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "handover_checklists")
public class HandoverChecklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "handover_record_id", nullable = false)
    private Long handoverRecordId;

    @Column(name = "checkpoint_name", nullable = false, length = 200)
    private String checkpointName;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "PENDING";

    @Column(name = "is_passed", nullable = false)
    private Boolean isPassed = false;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (sortOrder == null) {
            sortOrder = 0;
        }

        if (status == null) {
            status = "PENDING";
        }

        if (isPassed == null) {
            isPassed = false;
        }
    }

    public Long getId() {
        return id;
    }

    public Long getHandoverRecordId() {
        return handoverRecordId;
    }

    public void setHandoverRecordId(Long handoverRecordId) {
        this.handoverRecordId = handoverRecordId;
    }

    public String getCheckpointName() {
        return checkpointName;
    }

    public void setCheckpointName(String checkpointName) {
        this.checkpointName = checkpointName;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getIsPassed() {
        return isPassed;
    }

    public void setIsPassed(Boolean isPassed) {
        this.isPassed = isPassed;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

package com.equipmentrental.logistics.entity;

import com.equipmentrental.logistics.entity.enums.DispatchStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dispatch_notes")
public class DispatchNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "dispatch_code", nullable = false, unique = true, length = 50)
    private String dispatchCode;

    @Column(name = "rental_order_id", nullable = false)
    private Long rentalOrderId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "delivery_task_id", nullable = false)
    private Long deliveryTaskId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private DispatchStatus status = DispatchStatus.PREPARED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (status == null) {
            status = DispatchStatus.PREPARED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }

    public String getDispatchCode() {
        return dispatchCode;
    }

    public void setDispatchCode(String dispatchCode) {
        this.dispatchCode = dispatchCode;
    }

    public Long getRentalOrderId() {
        return rentalOrderId;
    }

    public void setRentalOrderId(Long rentalOrderId) {
        this.rentalOrderId = rentalOrderId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getDeliveryTaskId() {
        return deliveryTaskId;
    }

    public void setDeliveryTaskId(Long deliveryTaskId) {
        this.deliveryTaskId = deliveryTaskId;
    }

    public DispatchStatus getStatus() {
        return status;
    }

    public void setStatus(DispatchStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

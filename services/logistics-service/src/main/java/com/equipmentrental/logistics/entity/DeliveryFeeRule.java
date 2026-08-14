package com.equipmentrental.logistics.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_fee_rules")
public class DeliveryFeeRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID tham chiếu organization-customer-service
    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    // Có thể null nếu chính sách áp dụng toàn organization
    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "base_fee", nullable = false, precision = 15, scale = 2)
    private BigDecimal baseFee;

    @Column(name = "max_distance_km", nullable = false, precision = 10, scale = 2)
    private BigDecimal maxDistanceKm;

    @Column(name = "extra_fee_per_km", nullable = false, precision = 15, scale = 2)
    private BigDecimal extraFeePerKm;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

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

        if (isActive == null) {
            isActive = true;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getBaseFee() {
        return baseFee;
    }

    public void setBaseFee(BigDecimal baseFee) {
        this.baseFee = baseFee;
    }

    public BigDecimal getMaxDistanceKm() {
        return maxDistanceKm;
    }

    public void setMaxDistanceKm(BigDecimal maxDistanceKm) {
        this.maxDistanceKm = maxDistanceKm;
    }

    public BigDecimal getExtraFeePerKm() {
        return extraFeePerKm;
    }

    public void setExtraFeePerKm(BigDecimal extraFeePerKm) {
        this.extraFeePerKm = extraFeePerKm;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

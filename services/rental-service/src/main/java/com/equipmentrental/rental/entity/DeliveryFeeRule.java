package com.equipmentrental.rental.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_fee_rules")
public class DeliveryFeeRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(length = 120)
    private String area;
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal baseFee;
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal feePerKm;
    private Long equipmentTypeId;
    @Column(nullable = false)
    private Boolean active = true;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void pre() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void upd() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String v) {
        name = v;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String v) {
        area = v;
    }

    public BigDecimal getBaseFee() {
        return baseFee;
    }

    public void setBaseFee(BigDecimal v) {
        baseFee = v;
    }

    public BigDecimal getFeePerKm() {
        return feePerKm;
    }

    public void setFeePerKm(BigDecimal v) {
        feePerKm = v;
    }

    public Long getEquipmentTypeId() {
        return equipmentTypeId;
    }

    public void setEquipmentTypeId(Long v) {
        equipmentTypeId = v;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean v) {
        active = v;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

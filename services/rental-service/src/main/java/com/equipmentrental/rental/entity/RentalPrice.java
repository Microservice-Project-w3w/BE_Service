package com.equipmentrental.rental.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rental_prices")
public class RentalPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 150)
    private String priceName;
    @Column(nullable = false)
    private Long equipmentTypeId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RentalUnit rentalUnit;
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal rentalPrice;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DepositType depositType;
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal depositValue;
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal lateFee;
    @Column(nullable = false)
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    @Column(nullable = false)
    private Boolean active = true;
    @Column(length = 500)
    private String description;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = updatedAt = LocalDateTime.now();
        if (active == null)
            active = true;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getPriceName() {
        return priceName;
    }

    public void setPriceName(String v) {
        priceName = v;
    }

    public Long getEquipmentTypeId() {
        return equipmentTypeId;
    }

    public void setEquipmentTypeId(Long v) {
        equipmentTypeId = v;
    }

    public RentalUnit getRentalUnit() {
        return rentalUnit;
    }

    public void setRentalUnit(RentalUnit v) {
        rentalUnit = v;
    }

    public BigDecimal getRentalPrice() {
        return rentalPrice;
    }

    public void setRentalPrice(BigDecimal v) {
        rentalPrice = v;
    }

    public DepositType getDepositType() {
        return depositType;
    }

    public void setDepositType(DepositType v) {
        depositType = v;
    }

    public BigDecimal getDepositValue() {
        return depositValue;
    }

    public void setDepositValue(BigDecimal v) {
        depositValue = v;
    }

    public BigDecimal getLateFee() {
        return lateFee;
    }

    public void setLateFee(BigDecimal v) {
        lateFee = v;
    }

    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDateTime v) {
        validFrom = v;
    }

    public LocalDateTime getValidTo() {
        return validTo;
    }

    public void setValidTo(LocalDateTime v) {
        validTo = v;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean v) {
        active = v;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String v) {
        description = v;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

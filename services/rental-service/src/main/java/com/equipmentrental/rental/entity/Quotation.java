package com.equipmentrental.rental.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "quotations")
public class Quotation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long organizationId;
    @Column(nullable = false)
    private Long branchId;
    @Column(nullable = false, unique = true, length = 50)
    private String quotationCode;
    @Column(nullable = false)
    private Long rentalRequestId;
    @Column(nullable = false)
    private Long customerId;
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal rentalAmount;
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal depositAmount;
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal deliveryFee;
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal discountAmount;
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;
    @Column(length = 50)
    private String discountCode;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QuotationStatus status = QuotationStatus.DRAFT;
    @Column(nullable = false)
    private LocalDateTime validUntil;
    @Column(length = 1000)
    private String specialTerms;

    public Long getId() {
        return id;
    }

    public String getQuotationCode() {
        return quotationCode;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long value) {
        organizationId = value;
    }

    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long value) {
        branchId = value;
    }

    public void setQuotationCode(String v) {
        quotationCode = v;
    }

    public Long getRentalRequestId() {
        return rentalRequestId;
    }

    public void setRentalRequestId(Long v) {
        rentalRequestId = v;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long v) {
        customerId = v;
    }

    public BigDecimal getRentalAmount() {
        return rentalAmount;
    }

    public void setRentalAmount(BigDecimal v) {
        rentalAmount = v;
    }

    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(BigDecimal v) {
        depositAmount = v;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(BigDecimal v) {
        deliveryFee = v;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal v) {
        discountAmount = v;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal v) {
        totalAmount = v;
    }

    public String getDiscountCode() {
        return discountCode;
    }

    public void setDiscountCode(String v) {
        discountCode = v;
    }

    public QuotationStatus getStatus() {
        return status;
    }

    public void setStatus(QuotationStatus v) {
        status = v;
    }

    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDateTime v) {
        validUntil = v;
    }

    public String getSpecialTerms() {
        return specialTerms;
    }

    public void setSpecialTerms(String v) {
        specialTerms = v;
    }
}

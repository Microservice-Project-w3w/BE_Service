package com.equipmentrental.rental.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "discount_codes", uniqueConstraints = @UniqueConstraint(
        name = "uk_discount_code_organization_branch_code",
        columnNames = {"organization_id", "branch_id", "code"}
))
public class DiscountCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long organizationId;
    @Column(nullable = false)
    private Long branchId;
    @Column(nullable = false, length = 50)
    private String code;
    @Column(nullable = false, length = 150)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DiscountType discountType;
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal discountValue;
    @Column(precision = 15, scale = 2)
    private BigDecimal maxDiscount;
    @Column(precision = 15, scale = 2)
    private BigDecimal minOrderValue;
    @Column(length = 50)
    private String customerGroup;
    @Column(nullable = false)
    private LocalDateTime validFrom;
    @Column(nullable = false)
    private LocalDateTime validTo;
    @Column(nullable = false)
    private Boolean active = true;

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
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

    public void setCode(String v) {
        code = v;
    }

    public String getName() {
        return name;
    }

    public void setName(String v) {
        name = v;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public void setDiscountType(DiscountType v) {
        discountType = v;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal v) {
        discountValue = v;
    }

    public BigDecimal getMaxDiscount() {
        return maxDiscount;
    }

    public void setMaxDiscount(BigDecimal v) {
        maxDiscount = v;
    }

    public BigDecimal getMinOrderValue() {
        return minOrderValue;
    }

    public void setMinOrderValue(BigDecimal v) {
        minOrderValue = v;
    }

    public String getCustomerGroup() {
        return customerGroup;
    }

    public void setCustomerGroup(String v) {
        customerGroup = v;
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
}

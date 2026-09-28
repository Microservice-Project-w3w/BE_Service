package com.equipmentrental.rental.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rental_orders")
public class RentalOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long organizationId;

    @Column(nullable = false)
    private Long branchId;

    @Column(nullable = false, unique = true, length = 50)
    private String orderCode;

    @Column(nullable = false)
    private Long quotationId;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.PENDING;

    private LocalDateTime reservedUntil;

    @Column(length = 100)
    private String inventoryReservationId;

    @Column(length = 500)
    private String cancelReason;

    public Long getId() {
        return id;
    }

    public String getOrderCode() {
        return orderCode;
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

    public void setOrderCode(String v) {
        orderCode = v;
    }

    public Long getQuotationId() {
        return quotationId;
    }

    public void setQuotationId(Long v) {
        quotationId = v;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long v) {
        customerId = v;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(LocalDateTime v) {
        startAt = v;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(LocalDateTime v) {
        endAt = v;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal v) {
        totalAmount = v;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus v) {
        status = v;
    }

    public LocalDateTime getReservedUntil() {
        return reservedUntil;
    }

    public void setReservedUntil(LocalDateTime v) {
        reservedUntil = v;
    }

    public String getInventoryReservationId() {
        return inventoryReservationId;
    }

    public void setInventoryReservationId(String value) {
        inventoryReservationId = value;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String v) {
        cancelReason = v;
    }
}

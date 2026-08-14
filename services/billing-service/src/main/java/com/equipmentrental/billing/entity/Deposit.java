package com.equipmentrental.billing.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "deposits")
public class Deposit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "rental_order_id", nullable = false)
    private Long rentalOrderId;

    @Column(name = "rental_contract_id", nullable = false)
    private Long rentalContractId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "deducted_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal deductedAmount = BigDecimal.ZERO;

    @Column(name = "refunded_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    @Column(name = "payment_method", nullable = false, length = 30)
    private String paymentMethod;

    @Column(length = 100)
    private String reference;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false, length = 30)
    private String status = "HELD";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (deductedAmount == null) {
            deductedAmount = BigDecimal.ZERO;
        }

        if (refundedAmount == null) {
            refundedAmount = BigDecimal.ZERO;
        }

        if (status == null) {
            status = "HELD";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
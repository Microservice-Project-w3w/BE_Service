package com.equipmentrental.rental.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rental_contracts", uniqueConstraints = {
        @UniqueConstraint(name = "uk_rental_contract_order", columnNames = "rental_order_id"),
        @UniqueConstraint(name = "uk_rental_contract_code", columnNames = "contract_code")
})
public class RentalContract {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private Long organizationId;
    @Column(nullable = false) private Long branchId;
    @Column(nullable = false, unique = true, length = 50) private String contractCode;
    @Column(nullable = false) private Long rentalOrderId;
    @Column(nullable = false) private Long customerId;
    @Column(nullable = false) private LocalDateTime startAt;
    @Column(nullable = false) private LocalDateTime endAt;
    @Column(nullable = false, precision = 15, scale = 2) private BigDecimal totalAmount;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private ContractStatus status;
    @Column(length = 2000) private String terms;
    private LocalDateTime approvedAt;
    private LocalDateTime signedAt;
    private LocalDateTime liquidatedAt;
    @Column(length = 500) private String cancelReason;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    @Column(nullable = false) private LocalDateTime updatedAt;
    @PrePersist void prePersist() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate void preUpdate() { updatedAt = LocalDateTime.now(); }
    public Long getId() { return id; } public Long getOrganizationId() { return organizationId; } public void setOrganizationId(Long v) { organizationId = v; }
    public Long getBranchId() { return branchId; } public void setBranchId(Long v) { branchId = v; }
    public String getContractCode() { return contractCode; } public void setContractCode(String v) { contractCode = v; }
    public Long getRentalOrderId() { return rentalOrderId; } public void setRentalOrderId(Long v) { rentalOrderId = v; }
    public Long getCustomerId() { return customerId; } public void setCustomerId(Long v) { customerId = v; }
    public LocalDateTime getStartAt() { return startAt; } public void setStartAt(LocalDateTime v) { startAt = v; }
    public LocalDateTime getEndAt() { return endAt; } public void setEndAt(LocalDateTime v) { endAt = v; }
    public BigDecimal getTotalAmount() { return totalAmount; } public void setTotalAmount(BigDecimal v) { totalAmount = v; }
    public ContractStatus getStatus() { return status; } public void setStatus(ContractStatus v) { status = v; }
    public String getTerms() { return terms; } public void setTerms(String v) { terms = v; }
    public LocalDateTime getApprovedAt() { return approvedAt; } public void approve() { status = ContractStatus.APPROVED; approvedAt = LocalDateTime.now(); }
    public LocalDateTime getSignedAt() { return signedAt; } public void sign() { status = ContractStatus.SIGNED; signedAt = LocalDateTime.now(); }
    public LocalDateTime getLiquidatedAt() { return liquidatedAt; } public void liquidate() { status = ContractStatus.LIQUIDATED; liquidatedAt = LocalDateTime.now(); }
    public String getCancelReason() { return cancelReason; } public void cancel(String reason) { status = ContractStatus.CANCELLED; cancelReason = reason; }
}

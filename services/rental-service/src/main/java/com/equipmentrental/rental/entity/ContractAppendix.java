package com.equipmentrental.rental.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contract_appendices", uniqueConstraints = @UniqueConstraint(name = "uk_contract_appendix_code", columnNames = "appendix_code"))
public class ContractAppendix {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private Long organizationId;
    @Column(nullable = false) private Long branchId;
    @Column(nullable = false) private Long contractId;
    @Column(nullable = false, unique = true, length = 50) private String appendixCode;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private AppendixType appendixType;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private AppendixStatus status;
    private LocalDateTime newEndAt;
    @Column(nullable = false, length = 2000) private String terms;
    private LocalDateTime approvedAt;
    private LocalDateTime signedAt;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    @PrePersist void prePersist() { createdAt = LocalDateTime.now(); }
    public Long getId() { return id; } public Long getOrganizationId() { return organizationId; } public void setOrganizationId(Long v) { organizationId=v; }
    public Long getBranchId() { return branchId; } public void setBranchId(Long v) { branchId=v; } public Long getContractId(){return contractId;} public void setContractId(Long v){contractId=v;}
    public String getAppendixCode(){return appendixCode;} public void setAppendixCode(String v){appendixCode=v;} public AppendixType getAppendixType(){return appendixType;} public void setAppendixType(AppendixType v){appendixType=v;}
    public AppendixStatus getStatus(){return status;} public void setStatus(AppendixStatus v){status=v;} public LocalDateTime getNewEndAt(){return newEndAt;} public void setNewEndAt(LocalDateTime v){newEndAt=v;}
    public String getTerms(){return terms;} public void setTerms(String v){terms=v;} public LocalDateTime getApprovedAt(){return approvedAt;} public LocalDateTime getSignedAt(){return signedAt;}
    public void approve(){status=AppendixStatus.APPROVED;approvedAt=LocalDateTime.now();} public void sign(){status=AppendixStatus.SIGNED;signedAt=LocalDateTime.now();}
}

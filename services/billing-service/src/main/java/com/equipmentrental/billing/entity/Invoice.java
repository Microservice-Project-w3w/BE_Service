package com.equipmentrental.billing.entity;

import com.equipmentrental.billing.entity.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "contract_id", nullable = false)
    private Long contractId;

    @Column(name = "rental_order_id", nullable = false)
    private Long rentalOrderId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(
            name = "invoice_type",
            nullable = false,
            length = 30
    )
    private String invoiceType;

    @Column(
            name = "total_amount",
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InvoiceStatus status;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @OneToMany(
            mappedBy = "invoice",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<InvoiceItem> items =
            new ArrayList<>();

    @OneToMany(
            mappedBy = "invoice",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<IncurredFee> incurredFees =
            new ArrayList<>();

    @OneToMany(
            mappedBy = "invoice",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<Payment> payments =
            new ArrayList<>();

    @CreatedDate
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    @Column(name = "request_reference", unique = true, length = 100)
    private String requestReference;
}
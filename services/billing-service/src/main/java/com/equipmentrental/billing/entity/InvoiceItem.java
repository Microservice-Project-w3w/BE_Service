package com.equipmentrental.billing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "invoice_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "invoice_id",
            nullable = false
    )
    private Invoice invoice;

    @Column(
            name = "item_type",
            nullable = false,
            length = 30
    )
    private String itemType;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal quantity;

    @Column(
            name = "unit_price",
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal unitPrice;

    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal amount;

    @Column(
            name = "reference_type",
            length = 50
    )
    private String referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(
            name = "request_reference",
            unique = true,
            length = 100
    )
    private String requestReference;
}
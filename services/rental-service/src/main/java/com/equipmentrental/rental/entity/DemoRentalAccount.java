package com.equipmentrental.rental.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Minimal persistent demo billing; not a tax invoice or payment-provider integration. */
@Entity
@Table(name = "demo_rental_accounts")
public class DemoRentalAccount {
    @Id public Long rentalOrderId;
    public boolean invoiceIssued;
    public boolean invoiceCancelled;
    @Column(precision = 15, scale = 2) public BigDecimal invoiceAmount = BigDecimal.ZERO;
    @Column(precision = 15, scale = 2) public BigDecimal requiredDeposit = BigDecimal.ZERO;
    public LocalDate dueDate;
    @Column(length = 30) public String paymentMethod = "COD";
}

package com.equipmentrental.rental.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "demo_rental_events", uniqueConstraints = @UniqueConstraint(columnNames = {"rental_order_id", "request_key"}))
public class DemoRentalEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false) public Long rentalOrderId;
    @Column(nullable = false, length = 80) public String requestKey;
    @Column(nullable = false, length = 40) public String kind;
    @Column(precision = 15, scale = 2) public BigDecimal amount = BigDecimal.ZERO;
    @Column(length = 30) public String paymentMethod;
    @Column(length = 1000) public String note;
    @Column(columnDefinition = "LONGTEXT") public String details;
    @Column(nullable = false, length = 100) public String actorUserId;
    @Column(nullable = false) public LocalDateTime occurredAt = LocalDateTime.now();
}

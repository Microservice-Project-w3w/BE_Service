package com.equipmentrental.inventory.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name="demo_inventory_operations", uniqueConstraints={@UniqueConstraint(columnNames="request_key"),
        @UniqueConstraint(columnNames={"rental_order_id","kind"})})
public class DemoInventoryOperation {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false,length=80) public String requestKey;
    @Column(nullable=false) public Long organizationId;
    @Column(nullable=false) public Long branchId;
    @Column(nullable=false) public Long rentalOrderId;
    @Column(nullable=false) public Long reservationId;
    @Column(nullable=false,length=30) public String kind;
    @Column(nullable=false,columnDefinition="LONGTEXT") public String details;
    @Column(nullable=false,length=100) public String actorUserId;
    @Column(nullable=false) public LocalDateTime occurredAt=LocalDateTime.now();
}

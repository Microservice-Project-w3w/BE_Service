package com.equipmentrental.inventory.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name="demo_maintenance_tickets")
public class DemoMaintenanceTicket {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false) public Long organizationId;
    @Column(nullable=false) public Long branchId;
    @Column(nullable=false) public Long equipmentId;
    @Column(nullable=false) public LocalDateTime scheduledAt;
    @Column(nullable=false,length=30) public String status="PLANNED";
    @Column(nullable=false,length=1000) public String reason;
    @Column(length=1000) public String result;
    @Column(precision=15,scale=2) public BigDecimal cost=BigDecimal.ZERO;
    @Column(nullable=false,length=100) public String createdBy;
    @Column(length=100) public String completedBy;
    public LocalDateTime startedAt;
    public LocalDateTime completedAt;
}

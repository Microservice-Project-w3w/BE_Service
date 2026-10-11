package com.equipmentrental.rental.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name="demo_sync_tasks")
public class DemoSyncTask {
    @Id @Column(length=80) public String id;
    @Column(nullable=false) public Long rentalOrderId;
    @Column(nullable=false) public Long organizationId;
    @Column(nullable=false) public Long branchId;
    @Column(nullable=false,length=30) public String kind;
    @Column(nullable=false,length=30) public String status="PENDING";
    @Column(nullable=false,columnDefinition="LONGTEXT") public String payload;
    @Column(length=1000) public String lastError;
    public int attempts;
    @Column(nullable=false) public LocalDateTime createdAt=LocalDateTime.now();
    public LocalDateTime updatedAt=LocalDateTime.now();
}

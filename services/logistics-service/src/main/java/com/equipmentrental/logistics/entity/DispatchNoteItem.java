package com.equipmentrental.logistics.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "dispatch_note_items",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_dispatch_equipment",
                    columnNames = {"dispatch_note_id", "equipment_id"})
        })
public class DispatchNoteItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dispatch_note_id", nullable = false)
    private Long dispatchNoteId;

    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Long getDispatchNoteId() {
        return dispatchNoteId;
    }

    public void setDispatchNoteId(Long dispatchNoteId) {
        this.dispatchNoteId = dispatchNoteId;
    }

    public Long getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

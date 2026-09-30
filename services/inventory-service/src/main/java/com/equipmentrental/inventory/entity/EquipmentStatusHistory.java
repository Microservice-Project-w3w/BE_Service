package com.equipmentrental.inventory.entity;

import com.equipmentrental.inventory.enums.EquipmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "equipment_status_histories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentStatusHistory {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;


    @Enumerated(EnumType.STRING)
    @Column(name = "old_status")
    private EquipmentStatus oldStatus;


    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private EquipmentStatus newStatus;


    private String reason;


    @Column(name = "changed_by")
    private Long changedBy;


    @Column(name = "changed_at")
    private LocalDateTime changedAt;
}
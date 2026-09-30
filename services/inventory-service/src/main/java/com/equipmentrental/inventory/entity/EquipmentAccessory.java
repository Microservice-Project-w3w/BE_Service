package com.equipmentrental.inventory.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "equipment_accessories")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentAccessory {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;


    @Column(nullable = false)
    private String name;


    @Column(name = "serial_number")
    private String serialNumber;


    @Column(nullable = false)
    private Integer quantity = 1;


    @Column(name = "required_on_return")
    private Boolean requiredOnReturn = true;


    @Column(length = 500)
    private String note;


    @Column(name = "created_at")
    private LocalDateTime createdAt;


    @PrePersist
    public void prePersist(){

        createdAt = LocalDateTime.now();

    }

}
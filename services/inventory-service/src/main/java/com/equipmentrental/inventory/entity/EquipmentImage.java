package com.equipmentrental.inventory.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "equipment_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentImage {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;


    @Column(name = "image_url", nullable = false, length = 1000)
    private String imageUrl;


    @Column(name = "primary_image", nullable = false)
    private Boolean primaryImage = false;


    @Column(name = "display_order")
    private Integer displayOrder = 0;


    @Column(name = "created_at")
    private LocalDateTime createdAt;


    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
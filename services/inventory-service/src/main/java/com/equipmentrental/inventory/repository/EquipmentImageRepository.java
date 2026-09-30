package com.equipmentrental.inventory.repository;


import com.equipmentrental.inventory.entity.EquipmentImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface EquipmentImageRepository
        extends JpaRepository<EquipmentImage, Long> {


    List<EquipmentImage> findByEquipmentIdOrderByDisplayOrderAsc(
            Long equipmentId
    );


    void deleteByEquipmentId(
            Long equipmentId
    );
}
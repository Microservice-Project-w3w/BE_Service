package com.equipmentrental.inventory.repository;


import com.equipmentrental.inventory.entity.EquipmentAccessory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import java.util.List;


public interface EquipmentAccessoryRepository
        extends JpaRepository<EquipmentAccessory,Long> {


    List<EquipmentAccessory>
    findByEquipmentId(
            Long equipmentId
    );



    Optional<EquipmentAccessory>
    findByIdAndEquipmentId(
            Long id,
            Long equipmentId
    );

}
package com.equipmentrental.inventory.repository;


import com.equipmentrental.inventory.entity.EquipmentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface EquipmentStatusHistoryRepository
        extends JpaRepository<EquipmentStatusHistory,Long>{


    List<EquipmentStatusHistory>
    findByEquipmentIdOrderByChangedAtDesc(Long equipmentId);


}
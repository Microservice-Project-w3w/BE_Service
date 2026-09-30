package com.equipmentrental.inventory.repository;

import com.equipmentrental.inventory.entity.EquipmentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentTransactionRepository
        extends JpaRepository<EquipmentTransaction, Long> {

    List<EquipmentTransaction>
    findByEquipmentIdOrderByOccurredAtDesc(
            Long equipmentId
    );

    List<EquipmentTransaction>
    findByEquipmentIdAndOrganizationIdOrderByOccurredAtDesc(
            Long equipmentId,
            Long organizationId
    );
}
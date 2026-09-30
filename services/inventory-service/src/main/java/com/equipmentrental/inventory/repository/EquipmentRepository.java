package com.equipmentrental.inventory.repository;

import com.equipmentrental.inventory.entity.Equipment;
import com.equipmentrental.inventory.enums.EquipmentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    Optional<Equipment> findByEquipmentCode(String equipmentCode);

    boolean existsByEquipmentCode(String equipmentCode);

    List<Equipment> findByBranchIdAndStatus(
            Long branchId,
            EquipmentStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select e
            from Equipment e
            where e.id = :id
            """)
    Optional<Equipment> findByIdForUpdate(@Param("id") Long id);
}
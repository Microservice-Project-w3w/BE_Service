package com.equipmentrental.inventory.repository;

import com.equipmentrental.inventory.entity.EquipmentCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipmentCategoryRepository
        extends JpaRepository<EquipmentCategory, Long> {

    List<EquipmentCategory> findByOrganizationId(Long organizationId);

    Optional<EquipmentCategory> findByOrganizationIdAndCode(
            Long organizationId,
            String code
    );

    boolean existsByOrganizationIdAndCode(
            Long organizationId,
            String code
    );
}
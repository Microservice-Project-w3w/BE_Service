package com.equipmentrental.inventory.repository;

import com.equipmentrental.inventory.entity.EquipmentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipmentTypeRepository
        extends JpaRepository<EquipmentType, Long> {

    List<EquipmentType> findByOrganizationId(
            Long organizationId
    );

    List<EquipmentType> findByOrganizationIdAndCategoryId(
            Long organizationId,
            Long categoryId
    );

    Optional<EquipmentType> findByIdAndOrganizationId(
            Long id,
            Long organizationId
    );

    Optional<EquipmentType> findByOrganizationIdAndCode(
            Long organizationId,
            String code
    );

    boolean existsByOrganizationIdAndCode(
            Long organizationId,
            String code
    );
}
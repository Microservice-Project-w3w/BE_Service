package com.equipmentrental.inventory.repository;

import com.equipmentrental.inventory.entity.EquipmentModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipmentModelRepository
        extends JpaRepository<EquipmentModel, Long> {

    List<EquipmentModel> findByOrganizationId(
            Long organizationId
    );

    List<EquipmentModel> findByOrganizationIdAndEquipmentTypeId(
            Long organizationId,
            Long equipmentTypeId
    );

    List<EquipmentModel> findByOrganizationIdAndBrandId(
            Long organizationId,
            Long brandId
    );

    List<EquipmentModel> findByOrganizationIdAndEquipmentTypeIdAndBrandId(
            Long organizationId,
            Long equipmentTypeId,
            Long brandId
    );
    List<EquipmentModel> findByOrganizationIdAndEquipmentTypeIdAndActiveTrue(
            Long organizationId,
            Long equipmentTypeId
    );

    Optional<EquipmentModel> findByIdAndOrganizationId(
            Long id,
            Long organizationId
    );

    Optional<EquipmentModel> findByOrganizationIdAndCode(
            Long organizationId,
            String code
    );

    boolean existsByOrganizationIdAndCode(
            Long organizationId,
            String code
    );

}
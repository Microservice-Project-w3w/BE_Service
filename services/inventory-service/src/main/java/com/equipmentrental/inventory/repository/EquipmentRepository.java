package com.equipmentrental.inventory.repository;

import com.equipmentrental.inventory.entity.Equipment;
import com.equipmentrental.inventory.enums.EquipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface EquipmentRepository
        extends JpaRepository<Equipment, Long>,
        JpaSpecificationExecutor<Equipment> {

    Optional<Equipment> findByQrCode(String qrCode);
    List<Equipment> findByOrganizationId(
            Long organizationId
    );

    List<Equipment> findByOrganizationIdAndBranchId(
            Long organizationId,
            Long branchId
    );

    List<Equipment> findByOrganizationIdAndWarehouseId(
            Long organizationId,
            Long warehouseId
    );

    List<Equipment> findByOrganizationIdAndModelId(
            Long organizationId,
            Long modelId
    );

    List<Equipment> findByOrganizationIdAndStatus(
            Long organizationId,
            EquipmentStatus status
    );
    List<Equipment> findByOrganizationIdAndBranchIdAndStatus(
            Long organizationId,
            Long branchId,
            EquipmentStatus status
    );
    List<Equipment>
    findByOrganizationIdAndBranchIdAndWarehouseId(
            Long organizationId,
            Long branchId,
            Long warehouseId
    );
    List<Equipment> findByOrganizationIdAndBranchIdAndModelId(
            Long organizationId,
            Long branchId,
            Long modelId
    );
    List<Equipment> findByOrganizationIdAndBranchIdAndModelIdIn(
            Long organizationId,
            Long branchId,
            List<Long> modelIds
    );

    Optional<Equipment> findByIdAndOrganizationId(
            Long id,
            Long organizationId
    );

    Optional<Equipment> findByOrganizationIdAndSerialNumber(
            Long organizationId,
            String serialNumber
    );

    Optional<Equipment> findByOrganizationIdAndImei(
            Long organizationId,
            String imei
    );

    Optional<Equipment> findByOrganizationIdAndMacAddress(
            Long organizationId,
            String macAddress
    );

    boolean existsByOrganizationIdAndAssetCode(
            Long organizationId,
            String assetCode
    );

    boolean existsByOrganizationIdAndSerialNumber(
            Long organizationId,
            String serialNumber
    );

    boolean existsByOrganizationIdAndImei(
            Long organizationId,
            String imei
    );

    boolean existsByOrganizationIdAndMacAddress(
            Long organizationId,
            String macAddress
    );
}
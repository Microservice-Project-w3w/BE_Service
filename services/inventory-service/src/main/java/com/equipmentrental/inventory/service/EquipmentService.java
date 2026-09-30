package com.equipmentrental.inventory.service;

import com.equipmentrental.inventory.dto.request.CreateEquipmentRequest;
import com.equipmentrental.inventory.dto.request.UpdateEquipmentRequest;
import com.equipmentrental.inventory.dto.response.EquipmentResponse;
import com.equipmentrental.inventory.entity.Equipment;
import com.equipmentrental.inventory.enums.EquipmentStatus;
import com.equipmentrental.inventory.repository.EquipmentModelRepository;
import com.equipmentrental.inventory.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.equipmentrental.inventory.entity.EquipmentModel;
import com.equipmentrental.inventory.specification.EquipmentSpecification;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentModelRepository modelRepository;

    // =========================================================
    // CREATE EQUIPMENT
    // =========================================================

    @Transactional
    public EquipmentResponse create(
            CreateEquipmentRequest request
    ) {

        modelRepository
                .findByIdAndOrganizationId(
                        request.modelId(),
                        request.organizationId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy model thiết bị"
                        )
                );

        String assetCode =
                request.assetCode()
                        .trim()
                        .toUpperCase();

        if (equipmentRepository
                .existsByOrganizationIdAndAssetCode(
                        request.organizationId(),
                        assetCode
                )) {

            throw new IllegalArgumentException(
                    "Mã tài sản đã tồn tại"
            );
        }

        validateIdentifiers(
                request.organizationId(),
                null,
                request.serialNumber(),
                request.imei(),
                request.macAddress()
        );

        Equipment equipment =
                Equipment.builder()
                        .organizationId(
                                request.organizationId()
                        )
                        .branchId(
                                request.branchId()
                        )
                        .warehouseId(
                                request.warehouseId()
                        )
                        .warehouseLocationId(
                                request.warehouseLocationId()
                        )
                        .modelId(
                                request.modelId()
                        )
                        .assetCode(
                                assetCode
                        )
                        .serialNumber(
                                normalize(
                                        request.serialNumber()
                                )
                        )
                        .imei(
                                normalize(
                                        request.imei()
                                )
                        )
                        .macAddress(
                                normalize(
                                        request.macAddress()
                                )
                        )
                        .status(
                                request.status() == null
                                        ? EquipmentStatus.AVAILABLE
                                        : request.status()
                        )
                        .conditionStatus(
                                request.conditionStatus()
                        )
                        .purchaseDate(
                                request.purchaseDate()
                        )
                        .purchasePrice(
                                request.purchasePrice()
                        )
                        .note(
                                request.note()
                        )
                        .build();

        return toResponse(
                equipmentRepository.save(equipment)
        );
    }
    @Transactional(readOnly = true)
    public List<EquipmentResponse> search(
            Long organizationId,
            Long branchId,
            Long warehouseId,
            Long categoryId,
            Long equipmentTypeId,
            Long brandId,
            Long modelId,
            EquipmentStatus status,
            String keyword,
            String serialNumber,
            String imei,
            String macAddress
    ) {

        List<Long> modelIds = null;

        if (modelId != null) {

            modelIds = List.of(modelId);

        } else if (equipmentTypeId != null && brandId != null) {

            modelIds =
                    modelRepository
                            .findByOrganizationIdAndEquipmentTypeIdAndBrandId(
                                    organizationId,
                                    equipmentTypeId,
                                    brandId
                            )
                            .stream()
                            .map(EquipmentModel::getId)
                            .toList();

        } else if (equipmentTypeId != null) {

            modelIds =
                    modelRepository
                            .findByOrganizationIdAndEquipmentTypeId(
                                    organizationId,
                                    equipmentTypeId
                            )
                            .stream()
                            .map(EquipmentModel::getId)
                            .toList();

        } else if (brandId != null) {

            modelIds =
                    modelRepository
                            .findByOrganizationIdAndBrandId(
                                    organizationId,
                                    brandId
                            )
                            .stream()
                            .map(EquipmentModel::getId)
                            .toList();
        }

        Specification<Equipment> spec =
                Specification
                        .where(
                                EquipmentSpecification
                                        .organizationId(
                                                organizationId
                                        )
                        )
                        .and(
                                EquipmentSpecification
                                        .branchId(branchId)
                        )
                        .and(
                                EquipmentSpecification
                                        .warehouseId(warehouseId)
                        )
                        .and(
                                EquipmentSpecification
                                        .modelIds(modelIds)
                        )
                        .and(
                                EquipmentSpecification
                                        .status(status)
                        )
                        .and(
                                EquipmentSpecification
                                        .serialNumber(serialNumber)
                        )
                        .and(
                                EquipmentSpecification
                                        .imei(imei)
                        )
                        .and(
                                EquipmentSpecification
                                        .macAddress(macAddress)
                        );

        List<Equipment> equipment =
                equipmentRepository.findAll(spec);

        if (keyword != null && !keyword.isBlank()) {

            String normalizedKeyword =
                    keyword.trim().toUpperCase();

            equipment =
                    equipment.stream()
                            .filter(item ->
                                    contains(
                                            item.getAssetCode(),
                                            normalizedKeyword
                                    )
                                            ||
                                            contains(
                                                    item.getSerialNumber(),
                                                    normalizedKeyword
                                            )
                                            ||
                                            contains(
                                                    item.getImei(),
                                                    normalizedKeyword
                                            )
                                            ||
                                            contains(
                                                    item.getMacAddress(),
                                                    normalizedKeyword
                                            )
                            )
                            .toList();
        }

        return equipment
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // UPDATE EQUIPMENT
    // =========================================================

    @Transactional
    public EquipmentResponse update(
            Long id,
            Long organizationId,
            UpdateEquipmentRequest request
    ) {

        Equipment equipment =
                equipmentRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Không tìm thấy thiết bị"
                                )
                        );

        modelRepository
                .findByIdAndOrganizationId(
                        request.modelId(),
                        organizationId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy model thiết bị"
                        )
                );

        String assetCode =
                request.assetCode()
                        .trim()
                        .toUpperCase();

        if (!equipment.getAssetCode().equals(assetCode)
                && equipmentRepository
                .existsByOrganizationIdAndAssetCode(
                        organizationId,
                        assetCode
                )) {

            throw new IllegalArgumentException(
                    "Mã tài sản đã tồn tại"
            );
        }

        validateIdentifiers(
                organizationId,
                id,
                request.serialNumber(),
                request.imei(),
                request.macAddress()
        );

        equipment.setBranchId(
                request.branchId()
        );

        equipment.setWarehouseId(
                request.warehouseId()
        );

        equipment.setWarehouseLocationId(
                request.warehouseLocationId()
        );

        equipment.setModelId(
                request.modelId()
        );

        equipment.setAssetCode(
                assetCode
        );

        equipment.setSerialNumber(
                normalize(
                        request.serialNumber()
                )
        );

        equipment.setImei(
                normalize(
                        request.imei()
                )
        );

        equipment.setMacAddress(
                normalize(
                        request.macAddress()
                )
        );

        equipment.setConditionStatus(
                request.conditionStatus()
        );

        equipment.setPurchaseDate(
                request.purchaseDate()
        );

        equipment.setPurchasePrice(
                request.purchasePrice()
        );

        equipment.setNote(
                request.note()
        );

        return toResponse(
                equipmentRepository.save(equipment)
        );
    }

    // =========================================================
    // GET EQUIPMENT BY ID
    // =========================================================

    @Transactional(readOnly = true)
    public EquipmentResponse getById(
            Long id,
            Long organizationId
    ) {

        Equipment equipment =
                equipmentRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Không tìm thấy thiết bị"
                                )
                        );

        return toResponse(equipment);
    }

    // =========================================================
    // GET / FILTER EQUIPMENT
    // =========================================================

    @Transactional(readOnly = true)
    public List<EquipmentResponse> getAll(
            Long organizationId,
            Long branchId,
            Long warehouseId,
            Long modelId,
            EquipmentStatus status
    ) {

        List<Equipment> result;

        if (branchId != null) {

            result =
                    equipmentRepository
                            .findByOrganizationIdAndBranchId(
                                    organizationId,
                                    branchId
                            );

        } else if (warehouseId != null) {

            result =
                    equipmentRepository
                            .findByOrganizationIdAndWarehouseId(
                                    organizationId,
                                    warehouseId
                            );

        } else if (modelId != null) {

            result =
                    equipmentRepository
                            .findByOrganizationIdAndModelId(
                                    organizationId,
                                    modelId
                            );

        } else if (status != null) {

            result =
                    equipmentRepository
                            .findByOrganizationIdAndStatus(
                                    organizationId,
                                    status
                            );

        } else {

            result =
                    equipmentRepository
                            .findByOrganizationId(
                                    organizationId
                            );
        }

        return result
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // CHANGE EQUIPMENT STATUS
    // =========================================================

    @Transactional
    public EquipmentResponse changeStatus(
            Long id,
            Long organizationId,
            EquipmentStatus status
    ) {

        Equipment equipment =
                equipmentRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Không tìm thấy thiết bị"
                                )
                        );

        equipment.setStatus(status);

        return toResponse(
                equipmentRepository.save(equipment)
        );
    }

    // =========================================================
    // FIND EQUIPMENT BY SERIAL
    // =========================================================

    @Transactional(readOnly = true)
    public EquipmentResponse getBySerial(
            Long organizationId,
            String serial
    ) {

        String normalizedSerial =
                normalize(serial);

        Equipment equipment =
                equipmentRepository
                        .findByOrganizationIdAndSerialNumber(
                                organizationId,
                                normalizedSerial
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Không tìm thấy thiết bị theo serial"
                                )
                        );

        return toResponse(equipment);
    }

    // =========================================================
    // FIND EQUIPMENT BY IMEI
    // =========================================================

    @Transactional(readOnly = true)
    public EquipmentResponse getByImei(
            Long organizationId,
            String imei
    ) {

        String normalizedImei =
                normalize(imei);

        Equipment equipment =
                equipmentRepository
                        .findByOrganizationIdAndImei(
                                organizationId,
                                normalizedImei
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Không tìm thấy thiết bị theo IMEI"
                                )
                        );

        return toResponse(equipment);
    }

    // =========================================================
    // FIND EQUIPMENT BY MAC
    // =========================================================

    @Transactional(readOnly = true)
    public EquipmentResponse getByMac(
            Long organizationId,
            String mac
    ) {

        String normalizedMac =
                normalize(mac);

        Equipment equipment =
                equipmentRepository
                        .findByOrganizationIdAndMacAddress(
                                organizationId,
                                normalizedMac
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Không tìm thấy thiết bị theo MAC"
                                )
                        );

        return toResponse(equipment);
    }

    // =========================================================
    // VALIDATE SERIAL / IMEI / MAC
    // =========================================================

    private void validateIdentifiers(
            Long organizationId,
            Long currentEquipmentId,
            String serialNumber,
            String imei,
            String macAddress
    ) {

        String serial =
                normalize(serialNumber);

        String normalizedImei =
                normalize(imei);

        String mac =
                normalize(macAddress);

        if (serial != null) {

            equipmentRepository
                    .findByOrganizationIdAndSerialNumber(
                            organizationId,
                            serial
                    )
                    .filter(existing ->
                            currentEquipmentId == null
                                    || !existing.getId()
                                    .equals(currentEquipmentId)
                    )
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException(
                                "Serial number đã tồn tại"
                        );
                    });
        }

        if (normalizedImei != null) {

            equipmentRepository
                    .findByOrganizationIdAndImei(
                            organizationId,
                            normalizedImei
                    )
                    .filter(existing ->
                            currentEquipmentId == null
                                    || !existing.getId()
                                    .equals(currentEquipmentId)
                    )
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException(
                                "IMEI đã tồn tại"
                        );
                    });
        }

        if (mac != null) {

            equipmentRepository
                    .findByOrganizationIdAndMacAddress(
                            organizationId,
                            mac
                    )
                    .filter(existing ->
                            currentEquipmentId == null
                                    || !existing.getId()
                                    .equals(currentEquipmentId)
                    )
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException(
                                "MAC address đã tồn tại"
                        );
                    });
        }
    }

    // =========================================================
    // NORMALIZE STRING
    // =========================================================

    private String normalize(
            String value
    ) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value
                .trim()
                .toUpperCase();
    }

    // =========================================================
    // ENTITY -> RESPONSE DTO
    // =========================================================

    private EquipmentResponse toResponse(
            Equipment equipment
    ) {

        return new EquipmentResponse(
                equipment.getId(),
                equipment.getOrganizationId(),
                equipment.getBranchId(),
                equipment.getWarehouseId(),
                equipment.getWarehouseLocationId(),
                equipment.getModelId(),
                equipment.getAssetCode(),
                equipment.getSerialNumber(),
                equipment.getImei(),
                equipment.getMacAddress(),
                equipment.getQrCode(),
                equipment.getStatus(),
                equipment.getConditionStatus(),
                equipment.getPurchaseDate(),
                equipment.getPurchasePrice(),
                equipment.getNote(),
                equipment.getCreatedAt(),
                equipment.getUpdatedAt()
        );
    }
    private boolean contains(
            String value,
            String keyword
    ) {

        return value != null
                && value
                .toUpperCase()
                .contains(keyword);
    }
}
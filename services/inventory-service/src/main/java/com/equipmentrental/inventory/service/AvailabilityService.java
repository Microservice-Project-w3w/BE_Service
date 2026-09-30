package com.equipmentrental.inventory.service;

import com.equipmentrental.inventory.dto.response.EquipmentAvailabilityResponse;
import com.equipmentrental.inventory.entity.Equipment;
import com.equipmentrental.inventory.entity.EquipmentModel;
import com.equipmentrental.inventory.enums.EquipmentStatus;
import com.equipmentrental.inventory.repository.EquipmentModelRepository;
import com.equipmentrental.inventory.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentModelRepository equipmentModelRepository;

    @Transactional(readOnly = true)
    public EquipmentAvailabilityResponse checkAvailability(
            Long organizationId,
            Long branchId,
            Long equipmentTypeId,
            LocalDateTime startAt,
            LocalDateTime endAt,
            Integer quantity
    ) {

        validate(
                organizationId,
                branchId,
                equipmentTypeId,
                startAt,
                endAt,
                quantity
        );

        /*
         * 1. Tìm tất cả model thuộc equipmentTypeId.
         */
        List<EquipmentModel> models =
                equipmentModelRepository
                        .findByOrganizationIdAndEquipmentTypeIdAndActiveTrue(
                                organizationId,
                                equipmentTypeId
                        );

        if (models.isEmpty()) {

            return EquipmentAvailabilityResponse.builder()
                    .requestedQuantity(quantity)
                    .availableQuantity(0)
                    .available(false)
                    .equipmentIds(Collections.emptyList())
                    .build();
        }

        /*
         * 2. Lấy danh sách modelId.
         */
        List<Long> modelIds =
                models.stream()
                        .map(EquipmentModel::getId)
                        .toList();

        /*
         * 3. Tìm Equipment thuộc:
         * organization
         * branch
         * các model vừa tìm được.
         */
        List<Equipment> equipments =
                equipmentRepository
                        .findByOrganizationIdAndBranchIdAndModelIdIn(
                                organizationId,
                                branchId,
                                modelIds
                        );

        /*
         * 4. Lọc Equipment hiện có thể sử dụng.
         */
        List<Long> availableEquipmentIds =
                equipments.stream()

                        .filter(equipment ->
                                equipment.getStatus()
                                        == EquipmentStatus.AVAILABLE
                        )

                        .filter(equipment ->
                                equipment.getWarehouseId()
                                        != null
                        )

                        .map(Equipment::getId)

                        .toList();

        int availableQuantity =
                availableEquipmentIds.size();

        return EquipmentAvailabilityResponse.builder()

                .requestedQuantity(
                        quantity
                )

                .availableQuantity(
                        availableQuantity
                )

                .available(
                        availableQuantity
                                >= quantity
                )

                .equipmentIds(
                        availableEquipmentIds
                )

                .build();
    }

    private void validate(
            Long organizationId,
            Long branchId,
            Long equipmentTypeId,
            LocalDateTime startAt,
            LocalDateTime endAt,
            Integer quantity
    ) {

        if (organizationId == null) {
            throw new IllegalArgumentException(
                    "organizationId is required"
            );
        }

        if (branchId == null) {
            throw new IllegalArgumentException(
                    "branchId is required"
            );
        }

        if (equipmentTypeId == null) {
            throw new IllegalArgumentException(
                    "equipmentTypeId is required"
            );
        }

        if (startAt == null) {
            throw new IllegalArgumentException(
                    "startAt is required"
            );
        }

        if (endAt == null) {
            throw new IllegalArgumentException(
                    "endAt is required"
            );
        }

        if (!endAt.isAfter(startAt)) {
            throw new IllegalArgumentException(
                    "endAt must be after startAt"
            );
        }

        if (quantity == null
                || quantity <= 0) {

            throw new IllegalArgumentException(
                    "quantity must be greater than 0"
            );
        }
    }
}
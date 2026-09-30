package com.equipmentrental.inventory.service;

import com.equipmentrental.inventory.dto.response.EquipmentTransactionResponse;
import com.equipmentrental.inventory.entity.Equipment;
import com.equipmentrental.inventory.entity.EquipmentTransaction;
import com.equipmentrental.inventory.enums.EquipmentTransactionType;
import com.equipmentrental.inventory.exception.ResourceNotFoundException;
import com.equipmentrental.inventory.repository.EquipmentRepository;
import com.equipmentrental.inventory.repository.EquipmentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentTransactionService {

    private final EquipmentTransactionRepository repository;
    private final EquipmentRepository equipmentRepository;

    // =====================================================
    // READ HISTORY
    // =====================================================

    @Transactional(readOnly = true)
    public List<EquipmentTransactionResponse> getHistory(
            Long equipmentId
    ) {

        equipmentRepository
                .findById(equipmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Equipment not found: "
                                        + equipmentId
                        )
                );

        return repository
                .findByEquipmentIdOrderByOccurredAtDesc(
                        equipmentId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =====================================================
    // INTERNAL RECORD
    // =====================================================

    @Transactional
    public EquipmentTransaction record(

            Equipment equipment,

            EquipmentTransactionType type,

            Long fromWarehouseId,

            Long toWarehouseId,

            String referenceType,

            Long referenceId,

            String referenceCode,

            String oldStatus,

            String newStatus,

            Long performedBy,

            String note
    ) {

        EquipmentTransaction transaction =
                EquipmentTransaction.builder()

                        .equipmentId(
                                equipment.getId()
                        )

                        .organizationId(
                                equipment.getOrganizationId()
                        )

                        .branchId(
                                equipment.getBranchId()
                        )

                        .transactionType(type)

                        .fromWarehouseId(
                                fromWarehouseId
                        )

                        .toWarehouseId(
                                toWarehouseId
                        )

                        .referenceType(
                                referenceType
                        )

                        .referenceId(
                                referenceId
                        )

                        .referenceCode(
                                referenceCode
                        )

                        .oldStatus(
                                oldStatus
                        )

                        .newStatus(
                                newStatus
                        )

                        .performedBy(
                                performedBy
                        )

                        .note(
                                note
                        )

                        .build();

        return repository.save(
                transaction
        );
    }

    private EquipmentTransactionResponse toResponse(
            EquipmentTransaction entity
    ) {

        return new EquipmentTransactionResponse(

                entity.getId(),

                entity.getEquipmentId(),

                entity.getTransactionType(),

                entity.getBranchId(),

                entity.getFromWarehouseId(),

                entity.getToWarehouseId(),

                entity.getReferenceType(),

                entity.getReferenceId(),

                entity.getReferenceCode(),

                entity.getOldStatus(),

                entity.getNewStatus(),

                entity.getPerformedBy(),

                entity.getNote(),

                entity.getOccurredAt()
        );
    }
}
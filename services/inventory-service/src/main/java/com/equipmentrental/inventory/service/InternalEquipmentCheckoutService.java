package com.equipmentrental.inventory.service;

import com.equipmentrental.inventory.dto.request.CheckoutEquipmentRequest;
import com.equipmentrental.inventory.dto.response.CheckoutEquipmentResponse;
import com.equipmentrental.inventory.entity.Equipment;
import com.equipmentrental.inventory.enums.EquipmentStatus;
import com.equipmentrental.inventory.enums.EquipmentTransactionType;
import com.equipmentrental.inventory.exception.ResourceNotFoundException;
import com.equipmentrental.inventory.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InternalEquipmentCheckoutService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentTransactionService transactionService;

    @Transactional
    public CheckoutEquipmentResponse checkout(
            Long equipmentId,
            CheckoutEquipmentRequest request
    ) {

        validateRequest(request);

        /*
         * =====================================================
         * FIND EQUIPMENT
         * =====================================================
         */

        Equipment equipment =
                equipmentRepository
                        .findByIdAndOrganizationId(
                                equipmentId,
                                request.organizationId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Equipment not found: "
                                                + equipmentId
                                )
                        );

        /*
         * =====================================================
         * VALIDATE BRANCH
         * =====================================================
         */

        if (!equipment.getBranchId()
                .equals(request.branchId())) {

            throw new IllegalArgumentException(
                    "Equipment "
                            + equipment.getId()
                            + " does not belong to branch "
                            + request.branchId()
            );
        }

        /*
         * =====================================================
         * VALIDATE STATUS
         * =====================================================
         */

        if (equipment.getStatus()
                != EquipmentStatus.AVAILABLE) {

            throw new IllegalStateException(
                    "Equipment "
                            + equipment.getId()
                            + " is not AVAILABLE"
            );
        }

        /*
         * Thiết bị checkout phải đang thực sự nằm trong kho.
         */
        if (equipment.getWarehouseId() == null) {

            throw new IllegalStateException(
                    "Equipment "
                            + equipment.getId()
                            + " is not currently in warehouse"
            );
        }

        /*
         * =====================================================
         * SNAPSHOT OLD STATE
         * =====================================================
         */

        EquipmentStatus oldStatus =
                equipment.getStatus();

        Long oldWarehouseId =
                equipment.getWarehouseId();

        /*
         * =====================================================
         * CHECKOUT
         * =====================================================
         */

        equipment.setStatus(
                EquipmentStatus.CHECKED_OUT
        );

        equipment.setWarehouseId(
                null
        );

        equipment.setWarehouseLocationId(
                null
        );

        equipment =
                equipmentRepository.save(
                        equipment
                );

        /*
         * =====================================================
         * TRANSACTION HISTORY
         * =====================================================
         */

        transactionService.record(

                equipment,

                EquipmentTransactionType.CHECKOUT,

                oldWarehouseId,

                null,

                "RENTAL_ORDER",

                request.rentalOrderId(),

                request.checklistReference(),

                oldStatus.name(),

                equipment.getStatus().name(),

                request.actorUserId(),

                buildTransactionNote(
                        request
                )
        );

        /*
         * =====================================================
         * RESPONSE
         * =====================================================
         */

        return new CheckoutEquipmentResponse(

                equipment.getId(),

                request.rentalOrderId(),

                equipment.getOrganizationId(),

                equipment.getBranchId(),

                request.checklistReference(),

                oldStatus,

                equipment.getStatus(),

                request.actorUserId()
        );
    }

    private void validateRequest(
            CheckoutEquipmentRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "request is required"
            );
        }

        if (request.rentalOrderId() == null) {
            throw new IllegalArgumentException(
                    "rentalOrderId is required"
            );
        }

        if (request.organizationId() == null) {
            throw new IllegalArgumentException(
                    "organizationId is required"
            );
        }

        if (request.branchId() == null) {
            throw new IllegalArgumentException(
                    "branchId is required"
            );
        }

        if (request.checklistReference() == null
                || request.checklistReference().isBlank()) {

            throw new IllegalArgumentException(
                    "checklistReference is required"
            );
        }

        if (request.actorUserId() == null) {
            throw new IllegalArgumentException(
                    "actorUserId is required"
            );
        }
    }

    private String buildTransactionNote(
            CheckoutEquipmentRequest request
    ) {

        return "Equipment checkout for rental order "
                + request.rentalOrderId()
                + " - checklist "
                + request.checklistReference();
    }
}
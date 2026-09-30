package com.equipmentrental.inventory.service;

import com.equipmentrental.inventory.dto.request.CheckinEquipmentRequest;
import com.equipmentrental.inventory.dto.response.CheckinEquipmentResponse;
import com.equipmentrental.inventory.entity.Equipment;
import com.equipmentrental.inventory.enums.EquipmentCondition;
import com.equipmentrental.inventory.enums.EquipmentStatus;
import com.equipmentrental.inventory.enums.EquipmentTransactionType;
import com.equipmentrental.inventory.exception.ResourceNotFoundException;
import com.equipmentrental.inventory.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InternalEquipmentCheckinService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentTransactionService transactionService;

    @Transactional
    public CheckinEquipmentResponse checkin(
            Long equipmentId,
            CheckinEquipmentRequest request
    ) {

        validateRequest(request);

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
         * Phải đúng branch.
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
         * Chỉ equipment đang CHECKED_OUT
         * mới được checkin.
         */
        if (equipment.getStatus()
                != EquipmentStatus.CHECKED_OUT) {

            throw new IllegalStateException(
                    "Equipment "
                            + equipment.getId()
                            + " is not CHECKED_OUT"
            );
        }

        EquipmentStatus oldStatus =
                equipment.getStatus();

        /*
         * =====================================================
         * INVENTORY QUYẾT ĐỊNH TRẠNG THÁI CUỐI
         * =====================================================
         */

        EquipmentStatus newStatus =
                determineFinalStatus(
                        request.conditionStatus()
                );

        /*
         * Cập nhật condition thực tế.
         */
        equipment.setConditionStatus(
                request.conditionStatus()
        );

        equipment.setStatus(
                newStatus
        );

        /*
         * Chú ý:
         *
         * Checkout trước đó đã set warehouseId = null.
         *
         * Checkin thực tế phải biết thiết bị trả về kho nào.
         * Nhưng request #25 hiện tại không có warehouseId.
         *
         * Vì vậy tạm thời chưa set warehouseId ở đây.
         *
         * Nếu muốn đúng nghiệp vụ kho hoàn chỉnh,
         * nên bổ sung returnWarehouseId vào request.
         */

        equipment =
                equipmentRepository.save(
                        equipment
                );

        /*
         * =====================================================
         * GHI TRANSACTION HISTORY
         * =====================================================
         */

        transactionService.record(

                equipment,

                EquipmentTransactionType.CHECKIN,

                null,

                equipment.getWarehouseId(),

                "RENTAL_ORDER",

                request.rentalOrderId(),

                request.returnReportReference(),

                oldStatus.name(),

                newStatus.name(),

                request.actorUserId(),

                buildTransactionNote(
                        request,
                        newStatus
                )
        );

        return new CheckinEquipmentResponse(

                equipment.getId(),

                request.rentalOrderId(),

                equipment.getOrganizationId(),

                equipment.getBranchId(),

                request.returnReportReference(),

                oldStatus,

                newStatus,

                request.conditionStatus(),

                request.actorUserId()
        );
    }

    // =====================================================
    // DETERMINE FINAL STATUS
    // =====================================================

    private EquipmentStatus determineFinalStatus(
            EquipmentCondition conditionStatus
    ) {

        /*
         * Điều chỉnh các enum bên dưới
         * nếu EquipmentCondition hiện tại của bạn
         * dùng tên khác.
         */

        if (conditionStatus == EquipmentCondition.GOOD) {

            return EquipmentStatus.AVAILABLE;
        }

        /*
         * Các condition còn lại coi là cần kiểm tra/sửa chữa.
         */
        return EquipmentStatus.MAINTENANCE;
    }

    // =====================================================
    // VALIDATE REQUEST
    // =====================================================

    private void validateRequest(
            CheckinEquipmentRequest request
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

        if (request.returnReportReference() == null
                || request.returnReportReference().isBlank()) {

            throw new IllegalArgumentException(
                    "returnReportReference is required"
            );
        }

        if (request.conditionStatus() == null) {

            throw new IllegalArgumentException(
                    "conditionStatus is required"
            );
        }

        if (request.actorUserId() == null) {

            throw new IllegalArgumentException(
                    "actorUserId is required"
            );
        }
    }

    private String buildTransactionNote(
            CheckinEquipmentRequest request,
            EquipmentStatus newStatus
    ) {

        return "Equipment checkin for rental order "
                + request.rentalOrderId()
                + " - return report "
                + request.returnReportReference()
                + " - final status "
                + newStatus.name();
    }
}
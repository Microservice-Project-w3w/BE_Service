package com.equipmentrental.inventory.dto.response;

import com.equipmentrental.inventory.enums.EquipmentTransactionType;

import java.time.LocalDateTime;

public record EquipmentTransactionResponse(

        Long id,

        Long equipmentId,

        EquipmentTransactionType transactionType,

        Long branchId,

        Long fromWarehouseId,

        Long toWarehouseId,

        String referenceType,

        Long referenceId,

        String referenceCode,

        String oldStatus,

        String newStatus,

        Long performedBy,

        String note,

        LocalDateTime occurredAt

) {
}
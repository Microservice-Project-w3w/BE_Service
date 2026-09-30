package com.equipmentrental.inventory.dto.response;

import com.equipmentrental.inventory.enums.EquipmentCondition;
import com.equipmentrental.inventory.enums.EquipmentStatus;

public record CheckinEquipmentResponse(

        Long equipmentId,

        Long rentalOrderId,

        Long organizationId,

        Long branchId,

        String returnReportReference,

        EquipmentStatus oldStatus,

        EquipmentStatus newStatus,

        EquipmentCondition conditionStatus,

        Long actorUserId

) {
}
package com.equipmentrental.inventory.dto.request;

import com.equipmentrental.inventory.enums.EquipmentCondition;

public record CheckinEquipmentRequest(

        Long rentalOrderId,

        Long organizationId,

        Long branchId,

        String returnReportReference,

        EquipmentCondition conditionStatus,

        Long actorUserId

) {
}
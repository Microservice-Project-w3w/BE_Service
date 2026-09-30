package com.equipmentrental.inventory.dto.response;

import com.equipmentrental.inventory.enums.EquipmentCondition;
import com.equipmentrental.inventory.enums.EquipmentStatus;

public record InternalEquipmentResponse(

        Long equipmentId,

        Long organizationId,

        Long branchId,

        Long warehouseId,

        Long warehouseLocationId,

        Long equipmentTypeId,

        Long modelId,

        EquipmentStatus status,

        EquipmentCondition conditionStatus,

        String assetCode,

        String serialNumber,

        String imei,

        String macAddress,

        String qrCode

) {
}
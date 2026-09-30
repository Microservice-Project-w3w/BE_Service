package com.equipmentrental.inventory.dto.response;

import com.equipmentrental.inventory.enums.EquipmentCondition;
import com.equipmentrental.inventory.enums.EquipmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record EquipmentResponse(

        Long id,

        Long organizationId,

        Long branchId,

        Long warehouseId,

        Long warehouseLocationId,

        Long modelId,

        String assetCode,

        String serialNumber,

        String imei,

        String macAddress,

        String qrCode,

        EquipmentStatus status,

        EquipmentCondition conditionStatus,

        LocalDate purchaseDate,

        BigDecimal purchasePrice,

        String note,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}
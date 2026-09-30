package com.equipmentrental.inventory.dto.request;

import com.equipmentrental.inventory.enums.EquipmentCondition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateEquipmentRequest(

        @NotNull(message = "branchId không được để trống")
        Long branchId,

        Long warehouseId,

        Long warehouseLocationId,

        @NotNull(message = "modelId không được để trống")
        Long modelId,

        @NotBlank(message = "assetCode không được để trống")
        @Size(max = 100)
        String assetCode,

        @Size(max = 150)
        String serialNumber,

        @Size(max = 50)
        String imei,

        @Size(max = 50)
        String macAddress,

        EquipmentCondition conditionStatus,

        LocalDate purchaseDate,

        BigDecimal purchasePrice,

        @Size(max = 1000)
        String note
) {
}
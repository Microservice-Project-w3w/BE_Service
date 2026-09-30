package com.equipmentrental.inventory.dto.response;

import java.time.LocalDateTime;

public record EquipmentAccessoryResponse(

        Long id,

        Long equipmentId,

        String name,

        String serialNumber,

        Integer quantity,

        Boolean requiredOnReturn,

        String note,

        LocalDateTime createdAt

) {
}
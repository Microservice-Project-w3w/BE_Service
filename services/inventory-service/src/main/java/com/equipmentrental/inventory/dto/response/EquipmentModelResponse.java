package com.equipmentrental.inventory.dto.response;

import java.time.LocalDateTime;

public record EquipmentModelResponse(

        Long id,
        Long organizationId,
        Long equipmentTypeId,
        Long brandId,
        String code,
        String name,
        String manufacturerModel,
        String description,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
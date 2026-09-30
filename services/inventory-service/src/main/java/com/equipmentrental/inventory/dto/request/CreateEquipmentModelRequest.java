package com.equipmentrental.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateEquipmentModelRequest(

        @NotNull
        Long organizationId,

        @NotNull
        Long equipmentTypeId,

        @NotNull
        Long brandId,

        @NotBlank
        @Size(max = 50)
        String code,

        @NotBlank
        @Size(max = 150)
        String name,

        @Size(max = 150)
        String manufacturerModel,

        @Size(max = 1000)
        String description,

        Boolean active
) {
}
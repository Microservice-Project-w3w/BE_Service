package com.equipmentrental.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateBrandRequest(

        @NotBlank(message = "code không được để trống")
        @Size(max = 50)
        String code,

        @NotBlank(message = "name không được để trống")
        @Size(max = 150)
        String name,

        @Size(max = 500)
        String description,

        Boolean active
) {
}
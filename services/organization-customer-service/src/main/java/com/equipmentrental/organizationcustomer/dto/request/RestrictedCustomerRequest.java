package com.equipmentrental.organizationcustomer.dto.request;

import com.equipmentrental.organizationcustomer.enums.RestrictionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record RestrictedCustomerRequest(

        @NotNull(message = "customerId không được để trống")
        Long customerId,

        @NotNull(message = "Loại hạn chế không được để trống")
        RestrictionType restrictionType,

        @NotBlank(message = "Lý do hạn chế không được để trống")
        @Size(max = 1000)
        String reason,

        LocalDateTime restrictedFrom,

        LocalDateTime restrictedUntil,

        @NotNull(message = "restrictedByUserId không được để trống")
        Long restrictedByUserId

) {
}
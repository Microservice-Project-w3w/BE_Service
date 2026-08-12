package com.equipmentrental.organizationcustomer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RemoveRestrictionRequest(

        @NotNull(message = "removedByUserId không được để trống")
        Long removedByUserId,

        @NotBlank(message = "Lý do gỡ hạn chế không được để trống")
        @Size(max = 1000)
        String removedReason

) {
}
package com.equipmentrental.rental.dto;

import com.equipmentrental.rental.entity.RentalUnit;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RentalPriceResponse(
        Long id,
        String priceName,
        Long equipmentTypeId,
        RentalUnit rentalUnit,
        BigDecimal rentalPrice,
        BigDecimal depositAmount,
        BigDecimal lateFee,
        LocalDateTime validFrom,
        LocalDateTime validTo,
        Boolean active,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
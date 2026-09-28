package com.equipmentrental.rental.dto.response;

import com.equipmentrental.rental.entity.DepositType;
import com.equipmentrental.rental.entity.RentalUnit;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RentalPriceResponse(
        Long id,
        Long organizationId,
        Long branchId,
        String priceName,
        Long equipmentTypeId,
        RentalUnit rentalUnit,
        BigDecimal rentalPrice,
        DepositType depositType,
        BigDecimal depositValue,
        BigDecimal lateFee,
        LocalDateTime validFrom,
        LocalDateTime validTo,
        Boolean active,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}

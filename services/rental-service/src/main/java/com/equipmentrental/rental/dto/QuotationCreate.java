package com.equipmentrental.rental.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record QuotationCreate(
        @NotNull Long rentalRequestId,
        @NotNull @DecimalMin("0") BigDecimal rentalAmount,
        @NotNull @DecimalMin("0") BigDecimal depositAmount,
        @NotNull @DecimalMin("0") BigDecimal deliveryFee,
        String discountCode,
        @NotNull LocalDateTime validUntil,
        String specialTerms) {}

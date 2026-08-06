package com.equipmentrental.rental.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record QuotationCreate(@NotNull Long rentalRequestId, @NotNull BigDecimal rentalAmount,
                              @NotNull BigDecimal depositAmount,
                              @NotNull BigDecimal deliveryFee, String discountCode, @NotNull LocalDateTime validUntil, String specialTerms) {
}

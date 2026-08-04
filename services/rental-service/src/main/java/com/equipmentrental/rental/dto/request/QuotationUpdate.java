package com.equipmentrental.rental.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record QuotationUpdate(@NotNull @DecimalMin("0") BigDecimal rentalAmount,
                              @NotNull @DecimalMin("0") BigDecimal depositAmount,
                              @NotNull @DecimalMin("0") BigDecimal deliveryFee,
                              String discountCode, @NotNull LocalDateTime validUntil, String specialTerms) {
}

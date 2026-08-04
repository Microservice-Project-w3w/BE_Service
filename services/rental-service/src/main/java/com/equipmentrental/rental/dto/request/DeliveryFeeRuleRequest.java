package com.equipmentrental.rental.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record DeliveryFeeRuleRequest(@NotBlank String name, String area, @NotNull @DecimalMin("0") BigDecimal baseFee,
                                     @NotNull @DecimalMin("0") BigDecimal feePerKm, Long equipmentTypeId, Boolean active) {
}

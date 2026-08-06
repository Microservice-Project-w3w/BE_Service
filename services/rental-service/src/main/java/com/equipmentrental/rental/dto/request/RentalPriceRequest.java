package com.equipmentrental.rental.dto.request;

import com.equipmentrental.rental.entity.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RentalPriceRequest(@NotBlank String priceName, @NotNull Long organizationId, @NotNull Long branchId,
                                 @NotNull Long equipmentTypeId,
                                 @NotNull RentalUnit rentalUnit,
                                 @NotNull @DecimalMin("0") BigDecimal rentalPrice, @NotNull DepositType depositType,
                                 @NotNull @DecimalMin("0") BigDecimal depositValue,
                                 @NotNull @DecimalMin("0") BigDecimal lateFee, @NotNull LocalDateTime validFrom, LocalDateTime validTo,
                                 Boolean active, String description) {
}

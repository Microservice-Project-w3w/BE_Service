package com.equipmentrental.rental.dto.request;

import com.equipmentrental.rental.entity.DiscountType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DiscountCodeRequest(
        @NotBlank String code,
        @NotBlank String name,
        @NotNull Long organizationId,
        @NotNull Long branchId,
        @NotNull DiscountType discountType,
        @NotNull @DecimalMin("0") BigDecimal discountValue,
        BigDecimal maxDiscount,
        BigDecimal minOrderValue,
        String customerGroup,
        @NotNull LocalDateTime validFrom,
        @NotNull LocalDateTime validTo,
        Boolean active) {}

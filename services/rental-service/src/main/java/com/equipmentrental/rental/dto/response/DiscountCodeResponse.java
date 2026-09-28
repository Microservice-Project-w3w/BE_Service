package com.equipmentrental.rental.dto.response;

import com.equipmentrental.rental.entity.DiscountType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DiscountCodeResponse(
        Long id,
        Long organizationId,
        Long branchId,
        String code,
        String name,
        DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal maxDiscount,
        BigDecimal minOrderValue,
        String customerGroup,
        LocalDateTime validFrom,
        LocalDateTime validTo,
        Boolean active) {}

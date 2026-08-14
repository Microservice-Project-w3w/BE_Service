package com.equipmentrental.rental.dto.response;

import com.equipmentrental.rental.entity.QuotationStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record QuotationResponse(
        Long id,
        Long organizationId,
        Long branchId,
        String quotationCode,
        Long rentalRequestId,
        Long customerId,
        BigDecimal rentalAmount,
        BigDecimal depositAmount,
        BigDecimal deliveryFee,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        String discountCode,
        QuotationStatus status,
        LocalDateTime validUntil,
        String specialTerms) {}

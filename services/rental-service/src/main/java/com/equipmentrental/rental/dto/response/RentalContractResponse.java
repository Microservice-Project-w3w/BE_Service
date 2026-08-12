package com.equipmentrental.rental.dto.response;

import com.equipmentrental.rental.entity.ContractStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RentalContractResponse(
        Long id,
        Long organizationId,
        Long branchId,
        String contractCode,
        Long rentalOrderId,
        Long customerId,
        LocalDateTime startAt,
        LocalDateTime endAt,
        BigDecimal totalAmount,
        ContractStatus status,
        String terms,
        LocalDateTime approvedAt,
        LocalDateTime signedAt,
        LocalDateTime liquidatedAt,
        String cancelReason) {}

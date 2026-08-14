package com.equipmentrental.organizationcustomer.dto.response;

import com.equipmentrental.organizationcustomer.enums.RestrictionStatus;
import com.equipmentrental.organizationcustomer.enums.RestrictionType;

import java.time.LocalDateTime;

public record RestrictedCustomerResponse(

        Long id,

        Long organizationId,

        Long customerId,

        RestrictionType restrictionType,

        String reason,

        RestrictionStatus status,

        LocalDateTime restrictedFrom,

        LocalDateTime restrictedUntil,

        Long restrictedByUserId,

        LocalDateTime removedAt,

        Long removedByUserId,

        String removedReason,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}
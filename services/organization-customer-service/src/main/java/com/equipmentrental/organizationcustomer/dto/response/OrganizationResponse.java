package com.equipmentrental.organizationcustomer.dto.response;

import com.equipmentrental.organizationcustomer.enums.OrganizationStatus;

import java.time.LocalDateTime;

public record OrganizationResponse(

        Long id,

        String organizationCode,

        String organizationName,

        String taxCode,

        String email,

        String phone,

        String address,

        OrganizationStatus status,

        Long createdBy,

        Long updatedBy,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}
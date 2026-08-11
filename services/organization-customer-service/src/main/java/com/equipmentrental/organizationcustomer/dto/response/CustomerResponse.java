package com.equipmentrental.organizationcustomer.dto.response;

import com.equipmentrental.organizationcustomer.enums.CustomerStatus;
import com.equipmentrental.organizationcustomer.enums.CustomerType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CustomerResponse(

        Long id,

        Long organizationId,

        Long branchId,

        Long ownerUserId,

        String customerCode,

        CustomerType customerType,

        String displayName,

        String email,

        String phone,

        String address,


        // Khách cá nhân
        String fullName,

        LocalDate dateOfBirth,

        String identityNumber,


        // Khách doanh nghiệp
        String companyName,

        String taxCode,

        String representativeName,

        String representativePhone,

        String representativeEmail,


        CustomerStatus status,

        String note,

        Long createdBy,

        Long updatedBy,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}
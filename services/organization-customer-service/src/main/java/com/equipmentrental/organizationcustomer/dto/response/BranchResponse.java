package com.equipmentrental.organizationcustomer.dto.response;

import com.equipmentrental.organizationcustomer.enums.BranchStatus;

import java.time.LocalDateTime;

public record BranchResponse(

        Long id,

        Long organizationId,

        String branchCode,

        String branchName,

        String email,

        String phone,

        String address,

        BranchStatus status,

        Long createdBy,

        Long updatedBy,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}
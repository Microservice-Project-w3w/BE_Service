package com.equipmentrental.organizationcustomer.dto.request;

import com.equipmentrental.organizationcustomer.enums.BranchStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BranchRequest(
        @NotBlank @Size(max = 50) String branchCode,
        @NotBlank @Size(max = 255) String branchName,
        @Email @Size(max = 255) String email,
        @Size(max = 30) String phone,
        @Size(max = 500) String address,
        BranchStatus status,
        Long actorUserId
) {}

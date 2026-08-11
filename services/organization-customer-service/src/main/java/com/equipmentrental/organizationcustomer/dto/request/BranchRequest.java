package com.equipmentrental.organizationcustomer.dto.request;

import com.equipmentrental.organizationcustomer.enums.BranchStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BranchRequest(

        @NotBlank(message = "Mã chi nhánh không được để trống")
        @Size(max = 50)
        String branchCode,

        @NotBlank(message = "Tên chi nhánh không được để trống")
        @Size(max = 255)
        String branchName,

        @Email(message = "Email không đúng định dạng")
        @Size(max = 255)
        String email,

        @Size(max = 30)
        String phone,

        @Size(max = 500)
        String address,

        BranchStatus status,

        Long actorUserId

) {
}
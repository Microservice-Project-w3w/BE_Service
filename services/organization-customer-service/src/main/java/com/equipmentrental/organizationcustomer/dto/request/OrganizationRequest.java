package com.equipmentrental.organizationcustomer.dto.request;

import com.equipmentrental.organizationcustomer.enums.OrganizationStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrganizationRequest(

        @NotBlank(message = "Mã doanh nghiệp không được để trống")
        @Size(max = 50, message = "Mã doanh nghiệp tối đa 50 ký tự")
        String organizationCode,

        @NotBlank(message = "Tên doanh nghiệp không được để trống")
        @Size(max = 255, message = "Tên doanh nghiệp tối đa 255 ký tự")
        String organizationName,

        @Size(max = 50)
        String taxCode,

        @Email(message = "Email không đúng định dạng")
        @Size(max = 255)
        String email,

        @Size(max = 30)
        String phone,

        @Size(max = 500)
        String address,

        OrganizationStatus status,

        Long actorUserId

) {
}
package com.equipmentrental.organizationcustomer.dto.request;

import com.equipmentrental.organizationcustomer.enums.CustomerStatus;
import com.equipmentrental.organizationcustomer.enums.CustomerType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CustomerRequest(

        // Chi nhánh quản lý khách hàng
        Long branchId,

        // userId phụ trách khách hàng
        // chỉ tham chiếu identity-service
        Long ownerUserId,

        @NotBlank(message = "Mã khách hàng không được để trống")
        @Size(max = 50)
        String customerCode,

        @NotNull(message = "Loại khách hàng không được để trống")
        CustomerType customerType,

        @NotBlank(message = "Tên hiển thị không được để trống")
        @Size(max = 255)
        String displayName,

        @Email(message = "Email không đúng định dạng")
        @Size(max = 255)
        String email,

        @Size(max = 30)
        String phone,

        @Size(max = 500)
        String address,


        // =========================================
        // KHÁCH HÀNG CÁ NHÂN
        // =========================================

        @Size(max = 255)
        String fullName,

        LocalDate dateOfBirth,

        @Size(max = 100)
        String identityNumber,


        // =========================================
        // KHÁCH HÀNG DOANH NGHIỆP
        // =========================================

        @Size(max = 255)
        String companyName,

        @Size(max = 50)
        String taxCode,

        @Size(max = 255)
        String representativeName,

        @Size(max = 30)
        String representativePhone,

        @Email(message = "Email người đại diện không đúng định dạng")
        @Size(max = 255)
        String representativeEmail,


        CustomerStatus status,

        @Size(max = 1000)
        String note,

        // user đang thực hiện thao tác
        Long actorUserId

) {
}
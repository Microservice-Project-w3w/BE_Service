package com.equipmentrental.organizationcustomer.dto.request;

import com.equipmentrental.organizationcustomer.enums.EmployeeStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EmployeeRequest(

        /*
         * ID tham chiếu sang identity-service.
         * Không tạo User entity trong service này.
         */
        Long userId,

        @NotBlank(message = "Mã nhân viên không được để trống")
        @Size(max = 50)
        String employeeCode,

        @NotBlank(message = "Tên nhân viên không được để trống")
        @Size(max = 255)
        String fullName,

        @Email(message = "Email không đúng định dạng")
        @Size(max = 255)
        String email,

        @Size(max = 30)
        String phone,

        @Size(max = 100)
        String jobTitle,

        EmployeeStatus status,

        LocalDate hireDate,

        Long actorUserId

) {
}

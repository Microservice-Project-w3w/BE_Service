package com.equipmentrental.organizationcustomer.dto.request;

import com.equipmentrental.organizationcustomer.enums.EmployeeStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EmployeeRequest(
        Long userId,
        @NotBlank @Size(max = 50) String employeeCode,
        @NotBlank @Size(max = 255) String fullName,
        @Email @Size(max = 255) String email,
        @Size(max = 30) String phone,
        @Size(max = 100) String jobTitle,
        EmployeeStatus status,
        LocalDate hireDate,
        Long actorUserId
) {
}

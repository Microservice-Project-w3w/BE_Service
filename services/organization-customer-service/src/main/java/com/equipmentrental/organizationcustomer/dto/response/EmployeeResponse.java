package com.equipmentrental.organizationcustomer.dto.response;

import com.equipmentrental.organizationcustomer.enums.EmployeeStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EmployeeResponse(

        Long id,

        Long organizationId,

        Long userId,

        String employeeCode,

        String fullName,

        String email,

        String phone,

        String jobTitle,

        EmployeeStatus status,

        LocalDate hireDate,

        Long createdBy,

        Long updatedBy,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}

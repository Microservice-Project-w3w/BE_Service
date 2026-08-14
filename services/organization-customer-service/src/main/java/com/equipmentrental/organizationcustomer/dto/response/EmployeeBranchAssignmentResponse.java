package com.equipmentrental.organizationcustomer.dto.response;

import com.equipmentrental.organizationcustomer.enums.AssignmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EmployeeBranchAssignmentResponse(

        Long id,

        Long organizationId,

        Long employeeId,

        Long branchId,

        Boolean primaryAssignment,

        LocalDate assignedFrom,

        LocalDate assignedTo,

        AssignmentStatus status,

        Long createdBy,

        Long updatedBy,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}
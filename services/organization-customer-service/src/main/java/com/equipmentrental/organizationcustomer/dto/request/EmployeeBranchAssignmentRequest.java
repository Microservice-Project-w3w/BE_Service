package com.equipmentrental.organizationcustomer.dto.request;

import com.equipmentrental.organizationcustomer.enums.AssignmentStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record EmployeeBranchAssignmentRequest(

        @NotNull(message = "employeeId không được để trống")
        Long employeeId,

        @NotNull(message = "branchId không được để trống")
        Long branchId,

        Boolean primaryAssignment,

        LocalDate assignedFrom,

        LocalDate assignedTo,

        AssignmentStatus status,

        Long actorUserId

) {
}
package com.equipmentrental.organizationcustomer.controller;

import com.equipmentrental.organizationcustomer.dto.request.EmployeeBranchAssignmentRequest;
import com.equipmentrental.organizationcustomer.dto.response.EmployeeBranchAssignmentResponse;
import com.equipmentrental.organizationcustomer.service.EmployeeBranchAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping(
        "/api/v1/organizations/{organizationId}/employee-branch-assignments"
)
@RequiredArgsConstructor
public class EmployeeBranchAssignmentController {

    private final EmployeeBranchAssignmentService assignmentService;


    // =====================================================
    // 1. GÁN NHÂN VIÊN → CHI NHÁNH
    // =====================================================

    @PostMapping
    @PreAuthorize("hasAuthority('organization.employee.assign-branch') and @organizationScope.canAccessBranch(#organizationId, #request.branchId())")
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeBranchAssignmentResponse create(

            @PathVariable Long organizationId,

            @Valid
            @RequestBody EmployeeBranchAssignmentRequest request
    ) {

        return assignmentService.create(
                organizationId,
                request
        );
    }


    // =====================================================
    // 2. DANH SÁCH PHÂN CÔNG
    //
    // Có thể:
    // GET .../employee-branch-assignments
    //
    // hoặc:
    // GET .../employee-branch-assignments?employeeId=1
    // =====================================================

    @GetMapping
    @PreAuthorize("hasAuthority('organization.employee.read') and @organizationScope.canAccessOrganization(#organizationId)")
    public List<EmployeeBranchAssignmentResponse> getAll(

            @PathVariable Long organizationId,

            @RequestParam(required = false)
            Long employeeId
    ) {

        return assignmentService.getAll(
                organizationId,
                employeeId
        );
    }


    // =====================================================
    // 3. NGỪNG PHÂN CÔNG
    // =====================================================

    @PatchMapping("/{assignmentId}/deactivate")
    @PreAuthorize("hasAuthority('organization.employee.assign-branch') and @organizationScope.canAccessOrganization(#organizationId)")
    public EmployeeBranchAssignmentResponse deactivate(

            @PathVariable Long organizationId,

            @PathVariable Long assignmentId,

            @RequestParam(required = false)
            Long actorUserId
    ) {

        return assignmentService.deactivate(
                organizationId,
                assignmentId,
                actorUserId
        );
    }
}

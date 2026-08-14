package com.equipmentrental.organizationcustomer.controller;

import com.equipmentrental.organizationcustomer.dto.request.EmployeeRequest;
import com.equipmentrental.organizationcustomer.dto.response.EmployeeResponse;
import com.equipmentrental.organizationcustomer.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping(
        "/api/v1/organizations/{organizationId}/employees"
)
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;


    // =====================================================
    // 1. TẠO NHÂN VIÊN
    // =====================================================

    @PostMapping
    @PreAuthorize("hasAuthority('organization.employee.create') and @organizationScope.canAccessOrganization(#organizationId)")
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse create(

            @PathVariable Long organizationId,

            @Valid
            @RequestBody EmployeeRequest request
    ) {

        return employeeService.create(
                organizationId,
                request
        );
    }


    // =====================================================
    // 2. DANH SÁCH NHÂN VIÊN
    // =====================================================

    @GetMapping
    @PreAuthorize("hasAuthority('organization.employee.read') and @organizationScope.canAccessOrganization(#organizationId)")
    public List<EmployeeResponse> getAll(
            @PathVariable Long organizationId
    ) {

        return employeeService.getAll(
                organizationId
        );
    }


    // =====================================================
    // 3. CHI TIẾT NHÂN VIÊN
    // =====================================================

    @GetMapping("/{employeeId}")
    @PreAuthorize("hasAuthority('organization.employee.read') and @organizationScope.canAccessOrganization(#organizationId)")
    public EmployeeResponse getById(

            @PathVariable Long organizationId,

            @PathVariable Long employeeId
    ) {

        return employeeService.getById(
                organizationId,
                employeeId
        );
    }


    // =====================================================
    // 4. CẬP NHẬT NHÂN VIÊN
    // =====================================================

    @PutMapping("/{employeeId}")
    @PreAuthorize("hasAuthority('organization.employee.update') and @organizationScope.canAccessOrganization(#organizationId)")
    public EmployeeResponse update(

            @PathVariable Long organizationId,

            @PathVariable Long employeeId,

            @Valid
            @RequestBody EmployeeRequest request
    ) {

        return employeeService.update(
                organizationId,
                employeeId,
                request
        );
    }


    // =====================================================
    // 5. XÓA MỀM NHÂN VIÊN
    // =====================================================

    @DeleteMapping("/{employeeId}")
    @PreAuthorize("hasAuthority('organization.employee.update') and @organizationScope.canAccessOrganization(#organizationId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(

            @PathVariable Long organizationId,

            @PathVariable Long employeeId,

            @RequestParam(required = false)
            Long actorUserId
    ) {

        employeeService.delete(
                organizationId,
                employeeId,
                actorUserId
        );
    }
}

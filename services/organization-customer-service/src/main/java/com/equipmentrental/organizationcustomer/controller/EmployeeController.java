package com.equipmentrental.organizationcustomer.controller;

import com.equipmentrental.organizationcustomer.dto.request.EmployeeRequest;
import com.equipmentrental.organizationcustomer.dto.response.EmployeeResponse;
import com.equipmentrental.organizationcustomer.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasAuthority('organization.employee.create')")
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse create(@PathVariable Long organizationId,
                                   @Valid @RequestBody EmployeeRequest request) {
        return employeeService.create(organizationId, request);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('organization.employee.read')")
    public List<EmployeeResponse> getAll(@PathVariable Long organizationId) {
        return employeeService.getAll(organizationId);
    }

    @GetMapping("/{employeeId}")
    @PreAuthorize("hasAuthority('organization.employee.read')")
    public EmployeeResponse getById(@PathVariable Long organizationId, @PathVariable Long employeeId) {
        return employeeService.getById(organizationId, employeeId);
    }

    @PutMapping("/{employeeId}")
    @PreAuthorize("hasAuthority('organization.employee.update')")
    public EmployeeResponse update(@PathVariable Long organizationId, @PathVariable Long employeeId,
                                   @Valid @RequestBody EmployeeRequest request) {
        return employeeService.update(organizationId, employeeId, request);
    }

    @DeleteMapping("/{employeeId}")
    @PreAuthorize("hasAuthority('organization.employee.update')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long organizationId, @PathVariable Long employeeId,
                       @RequestParam(required = false) Long actorUserId) {
        employeeService.delete(organizationId, employeeId, actorUserId);
    }
}

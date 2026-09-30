package com.equipmentrental.organizationcustomer.controller;

import com.equipmentrental.organizationcustomer.dto.request.EmployeeRequest;
import com.equipmentrental.organizationcustomer.dto.response.EmployeeResponse;
import com.equipmentrental.organizationcustomer.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse create(@PathVariable Long organizationId,
                                   @Valid @RequestBody EmployeeRequest request) {
        return employeeService.create(organizationId, request);
    }

    @GetMapping
    public List<EmployeeResponse> getAll(@PathVariable Long organizationId) {
        return employeeService.getAll(organizationId);
    }

    @GetMapping("/{employeeId}")
    public EmployeeResponse getById(@PathVariable Long organizationId, @PathVariable Long employeeId) {
        return employeeService.getById(organizationId, employeeId);
    }

    @PutMapping("/{employeeId}")
    public EmployeeResponse update(@PathVariable Long organizationId, @PathVariable Long employeeId,
                                   @Valid @RequestBody EmployeeRequest request) {
        return employeeService.update(organizationId, employeeId, request);
    }
}

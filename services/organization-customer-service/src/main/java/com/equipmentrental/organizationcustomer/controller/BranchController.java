package com.equipmentrental.organizationcustomer.controller;

import com.equipmentrental.organizationcustomer.dto.request.BranchRequest;
import com.equipmentrental.organizationcustomer.dto.response.BranchResponse;
import com.equipmentrental.organizationcustomer.service.BranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/branches")
@RequiredArgsConstructor
public class BranchController {
    private final BranchService branchService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BranchResponse create(@PathVariable Long organizationId,
                                 @Valid @RequestBody BranchRequest request) {
        return branchService.create(organizationId, request);
    }

    @GetMapping
    public List<BranchResponse> getAll(@PathVariable Long organizationId) {
        return branchService.getAll(organizationId);
    }

    @GetMapping("/{branchId}")
    public BranchResponse getById(@PathVariable Long organizationId,
                                  @PathVariable Long branchId) {
        return branchService.getById(organizationId, branchId);
    }
}

package com.equipmentrental.billing.controller;

import com.equipmentrental.billing.dto.request.CreateDepositRequest;
import com.equipmentrental.billing.dto.response.DepositResponse;
import com.equipmentrental.billing.service.DepositService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import com.equipmentrental.billing.dto.request.CreateDepositDeductionRequest;
import com.equipmentrental.billing.dto.response.DepositDeductionResponse;
import com.equipmentrental.billing.dto.request.RefundDepositRequest;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/billing/deposits")
@RequiredArgsConstructor
public class DepositController {

    private final DepositService depositService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepositResponse createDeposit(
            @Valid @RequestBody CreateDepositRequest request
    ) {
        return depositService.createDeposit(request);
    }

    @GetMapping
    public List<DepositResponse> getAllDeposits() {
        return depositService.getAllDeposits();
    }

    @GetMapping("/{id}")
    public DepositResponse getDepositById(
            @PathVariable Long id
    ) {
        return depositService.getDepositById(id);
    }

    @PostMapping("/{id}/deductions")
    public DepositResponse deductDeposit(
            @PathVariable Long id,
            @Valid @RequestBody CreateDepositDeductionRequest request
    ) {
        return depositService.deductDeposit(id, request);
    }
    @GetMapping("/{id}/deductions")
    public List<DepositDeductionResponse> getDeductions(
            @PathVariable Long id
    ) {
        return depositService.getDeductions(id);
    }

    @PostMapping("/{id}/refund")
    public DepositResponse refundDeposit(
            @PathVariable Long id,
            @Valid @RequestBody RefundDepositRequest request
    ) {
        return depositService.refundDeposit(id, request);
    }

}
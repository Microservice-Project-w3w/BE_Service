package com.equipmentrental.rental.controller;

import com.equipmentrental.common.web.ApiResponse;
import com.equipmentrental.rental.service.DemoBillingService;
import com.equipmentrental.rental.service.DemoBillingService.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rental-orders")
public class DemoBillingController {
    private final DemoBillingService service;
    public DemoBillingController(DemoBillingService service) { this.service=service; }
    @GetMapping("/demo-accounts")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES_STAFF','ACCOUNTANT','CUSTOMER','OPERATIONS_STAFF')")
    public ApiResponse<List<Summary>> list(@RequestParam Long organizationId, @RequestParam Long branchId) {
        return ApiResponse.success(service.list(organizationId, branchId));
    }
    @GetMapping("/{id}/demo-account")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES_STAFF','ACCOUNTANT','CUSTOMER','OPERATIONS_STAFF')")
    public ApiResponse<Summary> get(@PathVariable Long id) { return ApiResponse.success(service.get(id)); }
    @PostMapping("/{id}/invoice")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ACCOUNTANT')")
    public ApiResponse<Summary> issue(@PathVariable Long id, @Valid @RequestBody InvoiceInput input) {
        return ApiResponse.success(service.issue(id,input));
    }
    public record Reason(@NotBlank @Size(max=1000) String reason) {}
    @PostMapping("/{id}/invoice/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ACCOUNTANT')")
    public ApiResponse<Summary> cancel(@PathVariable Long id, @Valid @RequestBody Reason input) {
        return ApiResponse.success(service.cancelInvoice(id,input.reason()));
    }
    @PutMapping("/{id}/payment-method")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES_STAFF','ACCOUNTANT','CUSTOMER')")
    public ApiResponse<Summary> method(@PathVariable Long id, @Valid @RequestBody MethodInput input) {
        return ApiResponse.success(service.method(id,input));
    }
    @PostMapping("/{id}/transactions/{kind}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ACCOUNTANT')")
    public ApiResponse<Summary> money(@PathVariable Long id, @PathVariable String kind,
            @Valid @RequestBody MoneyInput input) { return ApiResponse.success(service.money(id,kind,input)); }
}

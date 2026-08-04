package com.equipmentrental.rental.controller;

import com.equipmentrental.common.web.ApiResponse;
import com.equipmentrental.rental.dto.request.AppendixCreateRequest;
import com.equipmentrental.rental.dto.request.CancelContractRequest;
import com.equipmentrental.rental.dto.request.ContractCreateRequest;
import com.equipmentrental.rental.dto.request.ContractExtensionRequest;
import com.equipmentrental.rental.dto.response.ContractAppendixResponse;
import com.equipmentrental.rental.dto.response.RentalContractResponse;
import com.equipmentrental.rental.service.ContractService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rental-contracts")
public class ContractController {
    private final ContractService service;
    public ContractController(ContractService service) { this.service = service; }
    @PostMapping @PreAuthorize("hasAuthority('rental.contract.create')") public ResponseEntity<ApiResponse<RentalContractResponse>> create(@Valid @RequestBody ContractCreateRequest request) { return ResponseEntity.status(201).body(ApiResponse.success(service.create(request))); }
    @GetMapping @PreAuthorize("hasAuthority('rental.contract.read')") public ApiResponse<List<RentalContractResponse>> list(@RequestParam Long organizationId, @RequestParam Long branchId) { return ApiResponse.success(service.list(organizationId, branchId)); }
    @GetMapping("/{id}") @PreAuthorize("hasAuthority('rental.contract.read')") public ApiResponse<RentalContractResponse> get(@PathVariable Long id) { return ApiResponse.success(service.get(id)); }
    @PatchMapping("/{id}/approve") @PreAuthorize("hasAuthority('rental.contract.approve')") public ApiResponse<RentalContractResponse> approve(@PathVariable Long id) { return ApiResponse.success(service.approve(id)); }
    @PatchMapping("/{id}/sign") @PreAuthorize("hasAuthority('rental.contract.sign')") public ApiResponse<RentalContractResponse> sign(@PathVariable Long id) { return ApiResponse.success(service.sign(id)); }
    @PatchMapping("/{id}/cancel") @PreAuthorize("hasAuthority('rental.contract.cancel')") public ApiResponse<RentalContractResponse> cancel(@PathVariable Long id, @Valid @RequestBody CancelContractRequest request) { return ApiResponse.success(service.cancel(id, request)); }
    @PatchMapping("/{id}/liquidate") @PreAuthorize("hasAuthority('rental.contract.liquidate')") public ApiResponse<RentalContractResponse> liquidate(@PathVariable Long id) { return ApiResponse.success(service.liquidate(id)); }
    @PostMapping("/{id}/appendices") @PreAuthorize("hasAuthority('rental.contract.appendix.manage')") public ResponseEntity<ApiResponse<ContractAppendixResponse>> createAppendix(@PathVariable Long id, @Valid @RequestBody AppendixCreateRequest request) { return ResponseEntity.status(201).body(ApiResponse.success(service.createAppendix(id, request))); }
    @PostMapping("/{id}/extensions") @PreAuthorize("hasAuthority('rental.contract.extend')") public ResponseEntity<ApiResponse<ContractAppendixResponse>> createExtension(@PathVariable Long id, @Valid @RequestBody ContractExtensionRequest request) { return ResponseEntity.status(201).body(ApiResponse.success(service.createExtension(id, request))); }
    @GetMapping("/{id}/appendices") @PreAuthorize("hasAuthority('rental.contract.read')") public ApiResponse<List<ContractAppendixResponse>> appendices(@PathVariable Long id) { return ApiResponse.success(service.listAppendices(id)); }
    @PatchMapping("/appendices/{id}/approve") @PreAuthorize("hasAuthority('rental.contract.approve')") public ApiResponse<ContractAppendixResponse> approveAppendix(@PathVariable Long id) { return ApiResponse.success(service.approveAppendix(id)); }
    @PatchMapping("/appendices/{id}/sign") @PreAuthorize("hasAuthority('rental.contract.sign')") public ApiResponse<ContractAppendixResponse> signAppendix(@PathVariable Long id) { return ApiResponse.success(service.signAppendix(id)); }
}

package com.equipmentrental.rental.controller;
import com.equipmentrental.common.web.ApiResponse;
import com.equipmentrental.rental.service.*;
import com.equipmentrental.rental.service.DemoFulfillmentService.*;
import com.equipmentrental.rental.entity.DemoSyncTask;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/rental-orders/{id}")
public class DemoFulfillmentController {
    private final DemoFulfillmentService service;
    public DemoFulfillmentController(DemoFulfillmentService service) { this.service=service; }
    @PostMapping("/demo-signature")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES_STAFF','CUSTOMER')")
    public ApiResponse<DemoBillingService.Summary> sign(@PathVariable Long id,@Valid @RequestBody SignatureInput input) { return ApiResponse.success(service.sign(id,input)); }
    @PostMapping("/demo-liquidate")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<Void> liquidate(@PathVariable Long id) { service.liquidate(id); return ApiResponse.success(null); }
    @PostMapping("/handovers/{kind}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATIONS_STAFF')")
    public ApiResponse<DemoBillingService.Summary> handover(@PathVariable Long id,@PathVariable String kind,@Valid @RequestBody HandoverInput input) { return ApiResponse.success(service.handover(id,kind,input)); }
    @GetMapping("/sync-tasks")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATIONS_STAFF','SALES_STAFF')")
    public ApiResponse<List<DemoSyncTask>> tasks(@PathVariable Long id) { return ApiResponse.success(service.tasks(id)); }
    @PostMapping("/sync-tasks/{taskId}/retry")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATIONS_STAFF','SALES_STAFF')")
    public ApiResponse<Void> retry(@PathVariable Long id,@PathVariable String taskId) { service.recover(id,taskId); return ApiResponse.success(null); }
}

package com.equipmentrental.inventory.controller;
import com.equipmentrental.inventory.service.DemoInventoryService;
import com.equipmentrental.inventory.service.DemoInventoryService.*;
import com.equipmentrental.inventory.entity.*;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController
public class DemoInventoryController {
    private final DemoInventoryService service;
    public DemoInventoryController(DemoInventoryService service) { this.service=service; }
    @PostMapping("/internal/demo-handover")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATIONS_STAFF')")
    public DemoInventoryOperation handover(@Valid @RequestBody HandoverInput input) { return service.handover(input); }
    @GetMapping("/api/v1/inventory/maintenance")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATIONS_STAFF')")
    public List<DemoMaintenanceTicket> list(@RequestParam Long organizationId,@RequestParam Long branchId) { return service.list(organizationId,branchId); }
    @PostMapping("/api/v1/inventory/maintenance")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATIONS_STAFF')")
    public DemoMaintenanceTicket plan(@Valid @RequestBody MaintenanceInput input) { return service.plan(input); }
    @PostMapping("/api/v1/inventory/maintenance/{id}/{action}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATIONS_STAFF')")
    public DemoMaintenanceTicket action(@PathVariable Long id,@PathVariable String action,
            @Valid @RequestBody(required=false) MaintenanceResult input) { return service.transition(id,action,input); }
}

package com.equipmentrental.logistics.controller;

import com.equipmentrental.logistics.dto.request.CreateDeliveryTaskRequest;
import com.equipmentrental.logistics.dto.request.UpdateTaskStatusRequest;
import com.equipmentrental.logistics.dto.response.DeliveryTaskResponse;
import com.equipmentrental.logistics.service.DeliveryTaskService;
import com.equipmentrental.logistics.dto.request.UpdateDeliveryTaskRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/v1/logistics/delivery-tasks")
public class DeliveryTaskController {

    private final DeliveryTaskService service;

    public DeliveryTaskController(DeliveryTaskService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('logistics.delivery.create')")
    @ResponseStatus(HttpStatus.CREATED)
    public DeliveryTaskResponse createTask(
            @Valid @RequestBody CreateDeliveryTaskRequest request
    ) {
        return service.createTask(request);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('logistics.delivery.confirm')")
    public DeliveryTaskResponse updateTaskStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskStatusRequest request
    ) {
        return service.updateTaskStatus(
                id,
                request
        );
    }

    @GetMapping("/staff/{staffUserId}")
    @PreAuthorize("hasAuthority('logistics.delivery.read')")
    public List<DeliveryTaskResponse> getTasksByStaff(
            @PathVariable Long staffUserId
    ) {
        return service.getTasksByStaff(staffUserId);
    }

    @GetMapping("/schedule")
    @PreAuthorize("hasAuthority('logistics.delivery.read')")
    public List<DeliveryTaskResponse> getTasksByDate(
            @RequestParam String date
    ) {
        return service.getTasksByDate(date);
    }

    // LẤY TOÀN BỘ TASK
    @GetMapping
    @PreAuthorize("hasAuthority('logistics.delivery.read')")
    public List<DeliveryTaskResponse> getAllTasks() {
        return service.getAllTasks();
    }

    // LẤY CHI TIẾT TASK
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('logistics.delivery.read')")
    public DeliveryTaskResponse getTaskById(
            @PathVariable Long id
    ) {
        return service.getTaskById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('logistics.delivery.assign','logistics.delivery.schedule')")
    public DeliveryTaskResponse updateTask(
            @PathVariable Long id,
            @RequestBody UpdateDeliveryTaskRequest request
    ) {
        return service.updateTask(id, request);
    }
}

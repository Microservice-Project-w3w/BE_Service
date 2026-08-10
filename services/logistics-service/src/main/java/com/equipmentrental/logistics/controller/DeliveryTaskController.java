package com.equipmentrental.logistics.controller;

import com.equipmentrental.logistics.dto.request.CreateDeliveryTaskRequest;
import com.equipmentrental.logistics.dto.request.UpdateTaskStatusRequest;
import com.equipmentrental.logistics.dto.response.DeliveryTaskResponse;
import com.equipmentrental.logistics.service.DeliveryTaskService;
import com.equipmentrental.logistics.dto.request.UpdateDeliveryTaskRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/logistics/delivery-tasks")
public class DeliveryTaskController {

    private final DeliveryTaskService service;

    public DeliveryTaskController(DeliveryTaskService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeliveryTaskResponse createTask(
            @Valid @RequestBody CreateDeliveryTaskRequest request
    ) {
        return service.createTask(request);
    }

    @PutMapping("/{id}/status")
    public DeliveryTaskResponse updateTaskStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskStatusRequest request,
            @RequestHeader(
                    value = "X-User-Id",
                    required = false,
                    defaultValue = "1"
            ) Long currentUserId
    ) {
        return service.updateTaskStatus(
                id,
                request,
                currentUserId
        );
    }

    @GetMapping("/staff/{staffUserId}")
    public List<DeliveryTaskResponse> getTasksByStaff(
            @PathVariable Long staffUserId
    ) {
        return service.getTasksByStaff(staffUserId);
    }

    @GetMapping("/schedule")
    public List<DeliveryTaskResponse> getTasksByDate(
            @RequestParam String date
    ) {
        return service.getTasksByDate(date);
    }

    // LẤY TOÀN BỘ TASK
    @GetMapping
    public List<DeliveryTaskResponse> getAllTasks() {
        return service.getAllTasks();
    }

    // LẤY CHI TIẾT TASK
    @GetMapping("/{id}")
    public DeliveryTaskResponse getTaskById(
            @PathVariable Long id
    ) {
        return service.getTaskById(id);
    }

    @PutMapping("/{id}")
    public DeliveryTaskResponse updateTask(
            @PathVariable Long id,
            @RequestBody UpdateDeliveryTaskRequest request
    ) {
        return service.updateTask(id, request);
    }
}
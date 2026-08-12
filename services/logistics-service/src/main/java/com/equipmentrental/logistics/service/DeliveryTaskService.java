package com.equipmentrental.logistics.service;

import com.equipmentrental.logistics.dto.request.CreateDeliveryTaskRequest;
import com.equipmentrental.logistics.dto.request.UpdateTaskStatusRequest;
import com.equipmentrental.logistics.dto.response.DeliveryTaskResponse;
import com.equipmentrental.logistics.dto.request.UpdateDeliveryTaskRequest;
import com.equipmentrental.logistics.entity.DeliveryTask;
import com.equipmentrental.logistics.entity.enums.TaskStatus;

import com.equipmentrental.logistics.exception.ResourceNotFoundException;

import com.equipmentrental.logistics.repository.DeliveryTaskRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeliveryTaskService {

    private final DeliveryTaskRepository repository;

    public DeliveryTaskService(
            DeliveryTaskRepository repository
    ) {
        this.repository = repository;
    }

    // =====================================================
    // TẠO NHIỆM VỤ GIAO NHẬN
    // =====================================================

    @Transactional
    public DeliveryTaskResponse createTask(
            CreateDeliveryTaskRequest request
    ) {

        DeliveryTask task = new DeliveryTask();

        task.setRentalOrderId(
                request.getRentalOrderId()
        );

        task.setTaskType(
                request.getTaskType()
        );

        task.setDeliveryStaffUserId(
                request.getDeliveryStaffUserId()
        );

        task.setScheduledAt(
                request.getScheduledAt()
        );

        task.setStatus(
                TaskStatus.PENDING
        );

        return mapToResponse(
                repository.save(task)
        );
    }

    // =====================================================
    // CẬP NHẬT TRẠNG THÁI
    // =====================================================

    @Transactional
    public DeliveryTaskResponse updateTaskStatus(
            Long taskId,
            UpdateTaskStatusRequest request,
            Long currentUserId
    ) {

        DeliveryTask task =
                repository.findById(taskId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Delivery task not found: " + taskId
                                )
                        );

        // OWN scope
        if (task.getDeliveryStaffUserId() == null
                || !task.getDeliveryStaffUserId().equals(currentUserId)) {

            throw new RuntimeException(
                    "Unauthorized: You are not assigned to this delivery task"
            );
        }

        task.setStatus(
                request.getStatus()
        );

        return mapToResponse(
                repository.save(task)
        );
    }

    // =====================================================
    // LẤY TASK THEO NHÂN VIÊN
    // =====================================================

    @Transactional(readOnly = true)
    public List<DeliveryTaskResponse> getTasksByStaff(
            Long staffUserId
    ) {

        return repository
                .findByDeliveryStaffUserId(staffUserId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // =====================================================
    // LẤY TASK THEO NGÀY
    // =====================================================

    @Transactional(readOnly = true)
    public List<DeliveryTaskResponse> getTasksByDate(
            String date
    ) {

        LocalDate targetDate =
                LocalDate.parse(date);

        LocalDateTime start =
                targetDate.atStartOfDay();

        LocalDateTime end =
                targetDate
                        .plusDays(1)
                        .atStartOfDay();

        return repository
                .findByScheduledAtGreaterThanEqualAndScheduledAtLessThan(
                        start,
                        end
                )
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // =====================================================
    // LẤY TOÀN BỘ TASK
    // =====================================================

    @Transactional(readOnly = true)
    public List<DeliveryTaskResponse> getAllTasks() {

        return repository
                .findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // =====================================================
    // LẤY CHI TIẾT TASK
    // =====================================================

    @Transactional(readOnly = true)
    public DeliveryTaskResponse getTaskById(
            Long id
    ) {

        DeliveryTask task =
                repository.findById(id)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Delivery task not found: " + id
                                )
                        );

        return mapToResponse(task);
    }

    // =====================================================
    // MAP RESPONSE
    // =====================================================

    private DeliveryTaskResponse mapToResponse(
            DeliveryTask task
    ) {

        DeliveryTaskResponse res =
                new DeliveryTaskResponse();

        res.setId(
                task.getId()
        );

        res.setRentalOrderId(
                task.getRentalOrderId()
        );

        res.setTaskType(
                task.getTaskType()
        );

        res.setDeliveryStaffUserId(
                task.getDeliveryStaffUserId()
        );

        res.setScheduledAt(
                task.getScheduledAt()
        );

        res.setStatus(
                task.getStatus()
        );

        res.setCreatedAt(
                task.getCreatedAt()
        );

        res.setUpdatedAt(
                task.getUpdatedAt()
        );

        return res;
    }
    @Transactional
    public DeliveryTaskResponse updateTask(
            Long id,
            UpdateDeliveryTaskRequest request
    ) {

        DeliveryTask task = repository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Delivery task not found: " + id
                        )
                );

        if (request.getDeliveryStaffUserId() != null) {
            task.setDeliveryStaffUserId(
                    request.getDeliveryStaffUserId()
            );
        }

        if (request.getScheduledAt() != null) {
            task.setScheduledAt(
                    request.getScheduledAt()
            );
        }

        return mapToResponse(
                repository.save(task)
        );
    }
}
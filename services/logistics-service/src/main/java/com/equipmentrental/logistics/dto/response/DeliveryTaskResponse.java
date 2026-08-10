package com.equipmentrental.logistics.dto.response;

import com.equipmentrental.logistics.entity.enums.TaskStatus;
import com.equipmentrental.logistics.entity.enums.TaskType;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class DeliveryTaskResponse {
    private Long id;
    private Long rentalOrderId;
    private TaskType taskType;
    private Long deliveryStaffUserId;
    private LocalDateTime scheduledAt;
    private TaskStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

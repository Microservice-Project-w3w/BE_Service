package com.equipmentrental.logistics.dto.request;

import com.equipmentrental.logistics.entity.enums.TaskType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CreateDeliveryTaskRequest {
    @NotNull
    private Long organizationId;

    @NotNull
    private Long branchId;

    @NotNull
    private Long rentalOrderId;

    @NotNull
    private TaskType taskType;

    @NotNull
    private Long deliveryStaffUserId;

    @NotNull
    private LocalDateTime scheduledAt;
}

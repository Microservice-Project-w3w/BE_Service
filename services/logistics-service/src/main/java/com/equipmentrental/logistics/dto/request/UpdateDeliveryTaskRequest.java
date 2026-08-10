package com.equipmentrental.logistics.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateDeliveryTaskRequest {

    private Long deliveryStaffUserId;

    private LocalDateTime scheduledAt;

    private String notes;
}
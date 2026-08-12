package com.equipmentrental.logistics.dto.response;

import com.equipmentrental.logistics.entity.enums.ReturnInspectionStatus;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ReturnInspectionResponse {

    private Long id;
    private Long returnRequestId;
    private Long equipmentId;
    private Long inspectedByUserId;
    private LocalDateTime inspectedAt;
    private String conditionStatus;
    private String missingAccessories;
    private String damageDescription;
    private Boolean damaged;
    private Boolean late;
    private Long lateMinutes;
    private ReturnInspectionStatus status;
    private LocalDateTime createdAt;
}

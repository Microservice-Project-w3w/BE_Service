package com.equipmentrental.logistics.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CreateReturnInspectionRequest {

    @NotNull
    private Long returnRequestId;

    @NotNull
    private Long equipmentId;

    @NotNull
    private Long inspectedByUserId;

    @NotNull
    private LocalDateTime inspectedAt;

    @NotBlank
    private String conditionStatus;

    private String missingAccessories;

    private String damageDescription;

    private Boolean damaged;

    private Boolean late;

    private Long lateMinutes;
}

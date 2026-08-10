package com.equipmentrental.logistics.dto.request;

import com.equipmentrental.logistics.entity.enums.ConditionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReturnRecordItemRequest {
    @NotNull
    private Long equipmentId;

    @NotNull
    private ConditionStatus returnedCondition;

    private String notes;
}

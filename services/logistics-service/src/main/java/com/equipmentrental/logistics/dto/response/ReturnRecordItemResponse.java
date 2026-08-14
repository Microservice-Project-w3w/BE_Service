package com.equipmentrental.logistics.dto.response;

import com.equipmentrental.logistics.entity.enums.ConditionStatus;
import lombok.Data;

@Data
public class ReturnRecordItemResponse {
    private Long id;
    private Long equipmentId;
    private ConditionStatus returnedCondition;
    private String notes;
}

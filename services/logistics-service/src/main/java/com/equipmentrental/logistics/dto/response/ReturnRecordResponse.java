package com.equipmentrental.logistics.dto.response;

import com.equipmentrental.logistics.entity.enums.ReturnRecordStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class ReturnRecordResponse {
    private Long id;
    private Long returnRequestId;
    private Long rentalOrderId;
    private Long inspectorStaffUserId;
    private LocalDateTime actualReturnTime;
    private Boolean isLateReturn;
    private String missingAccessoriesDescription;
    private String conditionDamageDescription;
    private ReturnRecordStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<ReturnRecordItemResponse> items;
}

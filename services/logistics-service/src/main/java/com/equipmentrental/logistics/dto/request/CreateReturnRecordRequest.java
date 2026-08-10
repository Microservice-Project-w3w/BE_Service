package com.equipmentrental.logistics.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class CreateReturnRecordRequest {
    @NotNull
    private Long returnRequestId;

    @NotNull
    private Long rentalOrderId;

    @NotNull
    private Long inspectorStaffUserId;

    @NotNull
    private LocalDateTime actualReturnTime;

    @NotNull
    private Boolean isLateReturn;

    private String missingAccessoriesDescription;
    private String conditionDamageDescription;

    @Valid
    private List<ReturnRecordItemRequest> items;
}

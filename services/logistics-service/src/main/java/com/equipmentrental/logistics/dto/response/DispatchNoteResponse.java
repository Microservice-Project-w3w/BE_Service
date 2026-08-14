package com.equipmentrental.logistics.dto.response;

import com.equipmentrental.logistics.entity.enums.DispatchStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class DispatchNoteResponse {
    private Long id;
    private Long rentalOrderId;
    private Long customerId;
    private Long deliveryTaskId;
    private DispatchStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String dispatchCode;
    private List<DispatchNoteItemResponse> items;
}

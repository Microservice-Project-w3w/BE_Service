package com.equipmentrental.logistics.dto.response;

import com.equipmentrental.logistics.entity.enums.ReturnRequestStatus;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ReturnRequestResponse {
    private Long id;
    private Long rentalOrderId;
    private Long customerId;
    private LocalDateTime requestedReturnDate;
    private ReturnRequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

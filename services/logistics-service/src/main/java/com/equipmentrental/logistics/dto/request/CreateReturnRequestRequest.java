package com.equipmentrental.logistics.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CreateReturnRequestRequest {
    @NotNull
    private Long rentalOrderId;

    @NotNull
    private Long customerId;

    @NotNull
    private LocalDateTime requestedReturnDate;
}

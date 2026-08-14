package com.equipmentrental.billing.dto.external;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RentalOrderResponse {
    private Long id;
    private Long contractId;
    private Long customerId;
    private BigDecimal rentalPrice;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
}

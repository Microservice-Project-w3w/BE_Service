package com.equipmentrental.billing.dto.external;

import lombok.Data;

import java.util.List;

@Data
public class LogisticsReturnRecordResponse {
    private Long id;
    private Long rentalOrderId;
    private int lateDays;
    private List<String> missingAccessories;
}

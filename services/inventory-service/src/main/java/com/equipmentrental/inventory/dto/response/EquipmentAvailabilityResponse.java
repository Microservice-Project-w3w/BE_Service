package com.equipmentrental.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentAvailabilityResponse {

    private Integer requestedQuantity;

    private Integer availableQuantity;

    private Boolean available;

    private List<Long> equipmentIds;
}
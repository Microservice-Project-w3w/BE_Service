package com.equipmentrental.logistics.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HandoverChecklistRequest {

    @NotBlank
    private String checkpointName;

    private Integer sortOrder;

    private String status;

    @NotNull
    private Boolean isPassed;

    private String remarks;
}

package com.equipmentrental.logistics.dto.response;

import lombok.Data;

@Data
public class HandoverChecklistResponse {

    private Long id;

    private String checkpointName;

    private Integer sortOrder;

    private String status;

    private Boolean isPassed;

    private String remarks;
}

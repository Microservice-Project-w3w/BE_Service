package com.equipmentrental.organizationcustomer.dto.response;

import com.equipmentrental.organizationcustomer.enums.CustomerGroupStatus;

import java.time.LocalDateTime;

public record CustomerGroupResponse(

        Long id,

        Long organizationId,

        String groupCode,

        String groupName,

        String description,

        CustomerGroupStatus status,

        Long createdBy,

        Long updatedBy,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}
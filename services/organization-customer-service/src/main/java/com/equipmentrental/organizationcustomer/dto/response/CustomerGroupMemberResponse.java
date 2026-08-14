package com.equipmentrental.organizationcustomer.dto.response;

import java.time.LocalDateTime;

public record CustomerGroupMemberResponse(

        Long id,

        Long organizationId,

        Long customerGroupId,

        Long customerId,

        Long createdBy,

        LocalDateTime createdAt

) {
}
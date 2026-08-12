package com.equipmentrental.organizationcustomer.dto.request;

import com.equipmentrental.organizationcustomer.enums.CustomerGroupStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerGroupRequest(

        @NotBlank(message = "Mã nhóm khách hàng không được để trống")
        @Size(max = 50)
        String groupCode,

        @NotBlank(message = "Tên nhóm khách hàng không được để trống")
        @Size(max = 255)
        String groupName,

        @Size(max = 1000)
        String description,

        CustomerGroupStatus status,

        Long actorUserId

) {
}
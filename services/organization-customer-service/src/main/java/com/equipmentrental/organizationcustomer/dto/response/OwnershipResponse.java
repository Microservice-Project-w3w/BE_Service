package com.equipmentrental.organizationcustomer.dto.response;

public record OwnershipResponse(

        Long customerId,

        Long userId,

        boolean owned

) {
}
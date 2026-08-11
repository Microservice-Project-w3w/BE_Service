package com.equipmentrental.organizationcustomer.dto.response;

public record RestrictionCheckResponse(

        Long customerId,

        boolean restricted

) {
}
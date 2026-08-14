package com.equipmentrental.billing.dto.external;

import lombok.Data;

@Data
public class CustomerResponse {
    private Long id;
    private String name;
    private String email;
    private String taxCode;
    private String address;
}

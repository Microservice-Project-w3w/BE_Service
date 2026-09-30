package com.equipmentrental.organizationcustomer.controller;

import com.equipmentrental.organizationcustomer.dto.request.CustomerRequest;
import com.equipmentrental.organizationcustomer.dto.response.CustomerResponse;
import com.equipmentrental.organizationcustomer.enums.CustomerType;
import com.equipmentrental.organizationcustomer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/v1/organizations/{organizationId}/customers"
)
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;


    // =====================================================
    // 1. TẠO KHÁCH HÀNG
    // POST /api/v1/organizations/{organizationId}/customers
    // =====================================================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse create(

            @PathVariable Long organizationId,

            @Valid
            @RequestBody CustomerRequest request
    ) {

        return customerService.create(
                organizationId,
                request
        );
    }


    // =====================================================
    // 2. DANH SÁCH + LỌC + TÌM KIẾM
    //
    // GET /api/v1/organizations/{organizationId}/customers
    //
    // Có thể truyền:
    // ?branchId=1
    // ?customerType=INDIVIDUAL
    // ?customerType=BUSINESS
    // ?ownerUserId=10
    // ?q=nguyen
    // =====================================================

    @GetMapping
    public List<CustomerResponse> getAll(

            @PathVariable Long organizationId,

            @RequestParam(required = false)
            Long branchId,

            @RequestParam(required = false)
            CustomerType customerType,

            @RequestParam(required = false)
            Long ownerUserId,

            @RequestParam(required = false)
            String q
    ) {

        return customerService.getAll(
                organizationId,
                branchId,
                customerType,
                ownerUserId,
                q
        );
    }


    // =====================================================
    // 3. CHI TIẾT KHÁCH HÀNG
    //
    // GET
    // /api/v1/organizations/{organizationId}/customers/{customerId}
    // =====================================================

}

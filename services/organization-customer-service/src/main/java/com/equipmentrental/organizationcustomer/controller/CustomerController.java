package com.equipmentrental.organizationcustomer.controller;

import com.equipmentrental.organizationcustomer.dto.request.CustomerRequest;
import com.equipmentrental.organizationcustomer.dto.response.CustomerResponse;
import com.equipmentrental.organizationcustomer.dto.response.OwnershipResponse;
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

    @GetMapping("/{customerId}")
    public CustomerResponse getById(

            @PathVariable Long organizationId,

            @PathVariable Long customerId
    ) {

        return customerService.getById(
                organizationId,
                customerId
        );
    }


    // =====================================================
    // 4. CẬP NHẬT KHÁCH HÀNG
    //
    // PUT
    // /api/v1/organizations/{organizationId}/customers/{customerId}
    // =====================================================

    @PutMapping("/{customerId}")
    public CustomerResponse update(

            @PathVariable Long organizationId,

            @PathVariable Long customerId,

            @Valid
            @RequestBody CustomerRequest request
    ) {

        return customerService.update(
                organizationId,
                customerId,
                request
        );
    }


    // =====================================================
    // 5. XÓA MỀM KHÁCH HÀNG
    //
    // DELETE
    // /api/v1/organizations/{organizationId}/customers/{customerId}
    // =====================================================

    @DeleteMapping("/{customerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(

            @PathVariable Long organizationId,

            @PathVariable Long customerId,

            @RequestParam(required = false)
            Long actorUserId
    ) {

        customerService.delete(
                organizationId,
                customerId,
                actorUserId
        );
    }


    // =====================================================
    // 6. KIỂM TRA OWN
    //
    // GET
    // /api/v1/organizations/{organizationId}
    // /customers/{customerId}/ownership?userId=10
    // =====================================================

    @GetMapping("/{customerId}/ownership")
    public OwnershipResponse checkOwnership(

            @PathVariable Long organizationId,

            @PathVariable Long customerId,

            @RequestParam Long userId
    ) {

        return customerService.checkOwnership(
                organizationId,
                customerId,
                userId
        );
    }
}
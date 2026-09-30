package com.equipmentrental.organizationcustomer.service;

import com.equipmentrental.organizationcustomer.dto.request.RestrictedCustomerRequest;
import com.equipmentrental.organizationcustomer.dto.response.RestrictedCustomerResponse;
import com.equipmentrental.organizationcustomer.entity.RestrictedCustomer;
import com.equipmentrental.organizationcustomer.enums.RestrictionStatus;
import com.equipmentrental.organizationcustomer.exception.BadRequestException;
import com.equipmentrental.organizationcustomer.exception.ConflictException;
import com.equipmentrental.organizationcustomer.exception.NotFoundException;
import com.equipmentrental.organizationcustomer.repository.RestrictedCustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RestrictedCustomerService {

    private final RestrictedCustomerRepository repository;

    private final OrganizationService organizationService;

    private final CustomerService customerService;


    // =====================================================
    // 1. THÊM KHÁCH VÀO DANH SÁCH HẠN CHẾ
    // =====================================================

    public RestrictedCustomerResponse create(
            Long organizationId,
            RestrictedCustomerRequest request
    ) {

        organizationService.getEntity(organizationId);

        customerService.getEntity(
                organizationId,
                request.customerId()
        );

        LocalDateTime restrictedFrom =
                request.restrictedFrom() == null
                        ? LocalDateTime.now()
                        : request.restrictedFrom();


        if (request.restrictedUntil() != null
                && request.restrictedUntil()
                .isBefore(restrictedFrom)) {

            throw new BadRequestException(
                    "Thời điểm hết hạn không được trước thời điểm bắt đầu"
            );
        }


        if (repository
                .existsByOrganizationIdAndCustomerIdAndRestrictionTypeAndStatus(
                        organizationId,
                        request.customerId(),
                        request.restrictionType(),
                        RestrictionStatus.ACTIVE
                )) {

            throw new ConflictException(
                    "Khách hàng đang có hạn chế cùng loại"
            );
        }


        RestrictedCustomer restriction =
                RestrictedCustomer.builder()
                        .organizationId(organizationId)
                        .customerId(request.customerId())
                        .restrictionType(request.restrictionType())
                        .reason(request.reason().trim())
                        .status(RestrictionStatus.ACTIVE)
                        .restrictedFrom(restrictedFrom)
                        .restrictedUntil(request.restrictedUntil())
                        .restrictedByUserId(
                                request.restrictedByUserId()
                        )
                        .build();


        return toResponse(
                repository.save(restriction)
        );
    }



    private RestrictedCustomerResponse toResponse(
            RestrictedCustomer restriction
    ) {

        return new RestrictedCustomerResponse(
                restriction.getId(),
                restriction.getOrganizationId(),
                restriction.getCustomerId(),
                restriction.getRestrictionType(),
                restriction.getReason(),
                restriction.getStatus(),
                restriction.getRestrictedFrom(),
                restriction.getRestrictedUntil(),
                restriction.getRestrictedByUserId(),
                restriction.getRemovedAt(),
                restriction.getRemovedByUserId(),
                restriction.getRemovedReason(),
                restriction.getCreatedAt(),
                restriction.getUpdatedAt()
        );
    }
}

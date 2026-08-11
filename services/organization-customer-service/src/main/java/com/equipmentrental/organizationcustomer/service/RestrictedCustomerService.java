package com.equipmentrental.organizationcustomer.service;

import com.equipmentrental.organizationcustomer.dto.request.RemoveRestrictionRequest;
import com.equipmentrental.organizationcustomer.dto.request.RestrictedCustomerRequest;
import com.equipmentrental.organizationcustomer.dto.response.RestrictedCustomerResponse;
import com.equipmentrental.organizationcustomer.dto.response.RestrictionCheckResponse;
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


    // =====================================================
    // 2. DANH SÁCH / LỊCH SỬ
    // =====================================================

    @Transactional(readOnly = true)
    public List<RestrictedCustomerResponse> getAll(
            Long organizationId,
            Long customerId
    ) {

        organizationService.getEntity(organizationId);

        List<RestrictedCustomer> restrictions;

        if (customerId == null) {

            restrictions =
                    repository
                            .findAllByOrganizationIdOrderByIdDesc(
                                    organizationId
                            );

        } else {

            customerService.getEntity(
                    organizationId,
                    customerId
            );

            restrictions =
                    repository
                            .findAllByOrganizationIdAndCustomerIdOrderByIdDesc(
                                    organizationId,
                                    customerId
                            );
        }


        return restrictions
                .stream()
                .map(this::toResponse)
                .toList();
    }


    // =====================================================
    // 3. KIỂM TRA KHÁCH ĐANG BỊ HẠN CHẾ KHÔNG
    // =====================================================

    @Transactional(readOnly = true)
    public RestrictionCheckResponse check(
            Long organizationId,
            Long customerId
    ) {

        customerService.getEntity(
                organizationId,
                customerId
        );


        LocalDateTime now =
                LocalDateTime.now();


        boolean restricted =
                repository
                        .findAllByOrganizationIdAndCustomerIdAndStatus(
                                organizationId,
                                customerId,
                                RestrictionStatus.ACTIVE
                        )
                        .stream()
                        .anyMatch(
                                restriction -> {

                                    boolean started =
                                            restriction.getRestrictedFrom()
                                                    == null
                                                    || !restriction
                                                    .getRestrictedFrom()
                                                    .isAfter(now);

                                    boolean notExpired =
                                            restriction.getRestrictedUntil()
                                                    == null
                                                    || !restriction
                                                    .getRestrictedUntil()
                                                    .isBefore(now);

                                    return started && notExpired;
                                }
                        );


        return new RestrictionCheckResponse(
                customerId,
                restricted
        );
    }


    // =====================================================
    // 4. GỠ HẠN CHẾ
    // =====================================================

    public RestrictedCustomerResponse remove(
            Long organizationId,
            Long restrictionId,
            RemoveRestrictionRequest request
    ) {

        RestrictedCustomer restriction =
                repository
                        .findByIdAndOrganizationId(
                                restrictionId,
                                organizationId
                        )
                        .orElseThrow(
                                () -> new NotFoundException(
                                        "Không tìm thấy hạn chế id = "
                                                + restrictionId
                                )
                        );


        if (restriction.getStatus()
                != RestrictionStatus.ACTIVE) {

            throw new BadRequestException(
                    "Hạn chế này không còn ở trạng thái ACTIVE"
            );
        }


        restriction.setStatus(
                RestrictionStatus.REMOVED
        );

        restriction.setRemovedAt(
                LocalDateTime.now()
        );

        restriction.setRemovedByUserId(
                request.removedByUserId()
        );

        restriction.setRemovedReason(
                request.removedReason().trim()
        );


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
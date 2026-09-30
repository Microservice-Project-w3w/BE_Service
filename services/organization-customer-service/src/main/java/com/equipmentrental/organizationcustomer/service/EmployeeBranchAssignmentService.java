package com.equipmentrental.organizationcustomer.service;

import com.equipmentrental.organizationcustomer.dto.request.EmployeeBranchAssignmentRequest;
import com.equipmentrental.organizationcustomer.dto.response.EmployeeBranchAssignmentResponse;
import com.equipmentrental.organizationcustomer.entity.EmployeeBranchAssignment;
import com.equipmentrental.organizationcustomer.enums.AssignmentStatus;
import com.equipmentrental.organizationcustomer.exception.BadRequestException;
import com.equipmentrental.organizationcustomer.exception.ConflictException;
import com.equipmentrental.organizationcustomer.exception.NotFoundException;
import com.equipmentrental.organizationcustomer.repository.EmployeeBranchAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeBranchAssignmentService {

    private final EmployeeBranchAssignmentRepository assignmentRepository;

    private final OrganizationService organizationService;

    private final EmployeeService employeeService;

    private final BranchService branchService;


    // =====================================================
    // 1. GÁN NHÂN VIÊN VÀO CHI NHÁNH
    // =====================================================

    public EmployeeBranchAssignmentResponse create(

            Long organizationId,

            EmployeeBranchAssignmentRequest request
    ) {

        // Organization tồn tại
        organizationService.getEntity(
                organizationId
        );

        // Employee phải thuộc Organization này
        employeeService.getEntity(
                organizationId,
                request.employeeId()
        );

        // Branch phải thuộc Organization này
        branchService.getEntity(
                organizationId,
                request.branchId()
        );


        // Kiểm tra ngày
        if (request.assignedFrom() != null
                && request.assignedTo() != null
                && request.assignedTo()
                .isBefore(request.assignedFrom())) {

            throw new BadRequestException(
                    "Ngày kết thúc phân công không được trước ngày bắt đầu"
            );
        }


        // Không cho gán trùng
        if (assignmentRepository
                .existsByOrganizationIdAndEmployeeIdAndBranchId(
                        organizationId,
                        request.employeeId(),
                        request.branchId()
                )) {

            throw new ConflictException(
                    "Nhân viên đã được gán vào chi nhánh này"
            );
        }


        /*
         * Nếu assignment mới là primary,
         * bỏ primary của những assignment ACTIVE trước.
         */
        boolean primary =
                Boolean.TRUE.equals(
                        request.primaryAssignment()
                );


        if (primary) {

            List<EmployeeBranchAssignment> assignments =
                    assignmentRepository
                            .findAllByOrganizationIdAndEmployeeIdAndStatus(
                                    organizationId,
                                    request.employeeId(),
                                    AssignmentStatus.ACTIVE
                            );


            for (EmployeeBranchAssignment assignment
                    : assignments) {

                assignment.setPrimaryAssignment(
                        false
                );

                assignment.setUpdatedBy(
                        request.actorUserId()
                );
            }


            assignmentRepository.saveAll(
                    assignments
            );
        }


        EmployeeBranchAssignment assignment =
                EmployeeBranchAssignment.builder()

                        .organizationId(
                                organizationId
                        )

                        .employeeId(
                                request.employeeId()
                        )

                        .branchId(
                                request.branchId()
                        )

                        .primaryAssignment(
                                primary
                        )

                        .assignedFrom(
                                request.assignedFrom() == null
                                        ? LocalDate.now()
                                        : request.assignedFrom()
                        )

                        .assignedTo(
                                request.assignedTo()
                        )

                        .status(
                                request.status() == null
                                        ? AssignmentStatus.ACTIVE
                                        : request.status()
                        )

                        .createdBy(
                                request.actorUserId()
                        )

                        .updatedBy(
                                request.actorUserId()
                        )

                        .build();


        EmployeeBranchAssignment saved =
                assignmentRepository.save(
                        assignment
                );


        return toResponse(saved);
    }


    // =====================================================
    // ENTITY -> RESPONSE
    // =====================================================

    private EmployeeBranchAssignmentResponse toResponse(
            EmployeeBranchAssignment assignment
    ) {

        return new EmployeeBranchAssignmentResponse(

                assignment.getId(),

                assignment.getOrganizationId(),

                assignment.getEmployeeId(),

                assignment.getBranchId(),

                assignment.getPrimaryAssignment(),

                assignment.getAssignedFrom(),

                assignment.getAssignedTo(),

                assignment.getStatus(),

                assignment.getCreatedBy(),

                assignment.getUpdatedBy(),

                assignment.getCreatedAt(),

                assignment.getUpdatedAt()
        );
    }
}

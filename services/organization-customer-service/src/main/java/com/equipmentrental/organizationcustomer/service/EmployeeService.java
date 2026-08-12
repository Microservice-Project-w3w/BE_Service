package com.equipmentrental.organizationcustomer.service;

import com.equipmentrental.organizationcustomer.dto.request.EmployeeRequest;
import com.equipmentrental.organizationcustomer.dto.response.EmployeeResponse;
import com.equipmentrental.organizationcustomer.entity.Employee;
import com.equipmentrental.organizationcustomer.enums.EmployeeStatus;
import com.equipmentrental.organizationcustomer.exception.ConflictException;
import com.equipmentrental.organizationcustomer.exception.NotFoundException;
import com.equipmentrental.organizationcustomer.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    private final OrganizationService organizationService;


    // =====================================================
    // 1. TẠO NHÂN VIÊN
    // =====================================================

    public EmployeeResponse create(
            Long organizationId,
            EmployeeRequest request
    ) {

        // Organization phải tồn tại
        organizationService.getEntity(
                organizationId
        );

        // Không cho trùng employeeCode
        if (employeeRepository
                .existsByOrganizationIdAndEmployeeCodeAndDeletedAtIsNull(
                        organizationId,
                        request.employeeCode()
                )) {

            throw new ConflictException(
                    "Mã nhân viên đã tồn tại"
            );
        }


        /*
         * Nếu có userId:
         * một user không được map với 2 employee
         * trong cùng organization.
         */
        if (request.userId() != null
                && employeeRepository
                .existsByOrganizationIdAndUserIdAndDeletedAtIsNull(
                        organizationId,
                        request.userId()
                )) {

            throw new ConflictException(
                    "Tài khoản này đã được liên kết với nhân viên khác"
            );
        }


        Employee employee = Employee.builder()
                .organizationId(organizationId)
                .userId(request.userId())
                .employeeCode(request.employeeCode())
                .fullName(request.fullName())
                .email(clean(request.email()))
                .phone(clean(request.phone()))
                .jobTitle(clean(request.jobTitle()))
                .status(
                        request.status() == null
                                ? EmployeeStatus.ACTIVE
                                : request.status()
                )
                .hireDate(request.hireDate())
                .createdBy(request.actorUserId())
                .updatedBy(request.actorUserId())
                .build();


        Employee saved =
                employeeRepository.save(employee);

        return toResponse(saved);
    }


    // =====================================================
    // 2. DANH SÁCH NHÂN VIÊN
    // =====================================================

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAll(
            Long organizationId
    ) {

        organizationService.getEntity(
                organizationId
        );

        return employeeRepository
                .findAllByOrganizationIdAndDeletedAtIsNullOrderByIdDesc(
                        organizationId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }


    // =====================================================
    // 3. CHI TIẾT NHÂN VIÊN
    // =====================================================

    @Transactional(readOnly = true)
    public EmployeeResponse getById(
            Long organizationId,
            Long employeeId
    ) {

        return toResponse(
                getEntity(
                        organizationId,
                        employeeId
                )
        );
    }


    // =====================================================
    // 4. CẬP NHẬT NHÂN VIÊN
    // =====================================================

    public EmployeeResponse update(
            Long organizationId,
            Long employeeId,
            EmployeeRequest request
    ) {

        Employee employee =
                getEntity(
                        organizationId,
                        employeeId
                );


        // Kiểm tra employeeCode mới
        if (!employee.getEmployeeCode()
                .equals(request.employeeCode())

                && employeeRepository
                .existsByOrganizationIdAndEmployeeCodeAndDeletedAtIsNull(
                        organizationId,
                        request.employeeCode()
                )) {

            throw new ConflictException(
                    "Mã nhân viên đã tồn tại"
            );
        }


        // Kiểm tra userId mới
        if (request.userId() != null

                && !request.userId()
                .equals(employee.getUserId())

                && employeeRepository
                .existsByOrganizationIdAndUserIdAndDeletedAtIsNull(
                        organizationId,
                        request.userId()
                )) {

            throw new ConflictException(
                    "Tài khoản này đã được liên kết với nhân viên khác"
            );
        }


        employee.setUserId(
                request.userId()
        );

        employee.setEmployeeCode(
                request.employeeCode()
        );

        employee.setFullName(
                request.fullName()
        );

        employee.setEmail(
                clean(request.email())
        );

        employee.setPhone(
                clean(request.phone())
        );

        employee.setJobTitle(
                clean(request.jobTitle())
        );

        employee.setHireDate(
                request.hireDate()
        );


        if (request.status() != null) {
            employee.setStatus(
                    request.status()
            );
        }


        employee.setUpdatedBy(
                request.actorUserId()
        );


        Employee saved =
                employeeRepository.save(employee);

        return toResponse(saved);
    }


    // =====================================================
    // 5. XÓA MỀM NHÂN VIÊN
    // =====================================================

    public void delete(
            Long organizationId,
            Long employeeId,
            Long actorUserId
    ) {

        Employee employee =
                getEntity(
                        organizationId,
                        employeeId
                );


        employee.setStatus(
                EmployeeStatus.DELETED
        );

        employee.setDeletedAt(
                LocalDateTime.now()
        );

        employee.setUpdatedBy(
                actorUserId
        );


        employeeRepository.save(employee);
    }


    // =====================================================
    // 6. LẤY ENTITY NỘI BỘ
    // =====================================================

    public Employee getEntity(
            Long organizationId,
            Long employeeId
    ) {

        return employeeRepository
                .findByIdAndOrganizationIdAndDeletedAtIsNull(
                        employeeId,
                        organizationId
                )
                .orElseThrow(
                        () -> new NotFoundException(
                                "Không tìm thấy nhân viên id = "
                                        + employeeId
                        )
                );
    }


    // =====================================================
    // 7. ENTITY -> RESPONSE
    // =====================================================

    private EmployeeResponse toResponse(
            Employee employee
    ) {

        return new EmployeeResponse(

                employee.getId(),

                employee.getOrganizationId(),

                employee.getUserId(),

                employee.getEmployeeCode(),

                employee.getFullName(),

                employee.getEmail(),

                employee.getPhone(),

                employee.getJobTitle(),

                employee.getStatus(),

                employee.getHireDate(),

                employee.getCreatedBy(),

                employee.getUpdatedBy(),

                employee.getCreatedAt(),

                employee.getUpdatedAt()
        );
    }


    private String clean(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
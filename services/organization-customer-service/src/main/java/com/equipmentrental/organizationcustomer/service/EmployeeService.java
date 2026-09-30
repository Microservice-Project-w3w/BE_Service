package com.equipmentrental.organizationcustomer.service;

import com.equipmentrental.organizationcustomer.dto.request.EmployeeRequest;
import com.equipmentrental.organizationcustomer.dto.response.EmployeeResponse;
import com.equipmentrental.organizationcustomer.entity.Employee;
import com.equipmentrental.organizationcustomer.enums.EmployeeStatus;
import com.equipmentrental.organizationcustomer.exception.ConflictException;
import com.equipmentrental.organizationcustomer.exception.NotFoundException;
import com.equipmentrental.organizationcustomer.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final OrganizationService organizationService;

    public EmployeeResponse create(Long organizationId, EmployeeRequest request) {
        organizationService.getEntity(organizationId);
        if (employeeRepository.existsByOrganizationIdAndEmployeeCodeAndDeletedAtIsNull(
                organizationId, request.employeeCode())) {
            throw new ConflictException("Mã nhân viên đã tồn tại");
        }
        if (request.userId() != null && employeeRepository
                .existsByOrganizationIdAndUserIdAndDeletedAtIsNull(organizationId, request.userId())) {
            throw new ConflictException("Tài khoản đã được liên kết với nhân viên khác");
        }
        Employee employee = Employee.builder().organizationId(organizationId).userId(request.userId())
                .employeeCode(request.employeeCode().trim()).fullName(request.fullName().trim())
                .email(clean(request.email())).phone(clean(request.phone())).jobTitle(clean(request.jobTitle()))
                .status(request.status() == null ? EmployeeStatus.ACTIVE : request.status())
                .hireDate(request.hireDate()).createdBy(request.actorUserId()).updatedBy(request.actorUserId()).build();
        try {
            return toResponse(employeeRepository.save(employee));
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Mã nhân viên hoặc tài khoản đã tồn tại");
        }
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAll(Long organizationId) {
        organizationService.getEntity(organizationId);
        return employeeRepository.findAllByOrganizationIdAndDeletedAtIsNullOrderByIdDesc(organizationId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getById(Long organizationId, Long employeeId) {
        return toResponse(getEntity(organizationId, employeeId));
    }

    public Employee getEntity(Long organizationId, Long employeeId) {
        return employeeRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(employeeId, organizationId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy nhân viên id = " + employeeId));
    }

    private EmployeeResponse toResponse(Employee employee) {
        return new EmployeeResponse(employee.getId(), employee.getOrganizationId(), employee.getUserId(),
                employee.getEmployeeCode(), employee.getFullName(), employee.getEmail(), employee.getPhone(),
                employee.getJobTitle(), employee.getStatus(), employee.getHireDate(), employee.getCreatedBy(),
                employee.getUpdatedBy(), employee.getCreatedAt(), employee.getUpdatedAt());
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

package com.equipmentrental.organizationcustomer.repository;

import com.equipmentrental.organizationcustomer.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository
        extends JpaRepository<Employee, Long> {

    Optional<Employee>
    findByIdAndOrganizationIdAndDeletedAtIsNull(
            Long id,
            Long organizationId
    );

    List<Employee>
    findAllByOrganizationIdAndDeletedAtIsNullOrderByIdDesc(
            Long organizationId
    );

    boolean
    existsByOrganizationIdAndEmployeeCodeAndDeletedAtIsNull(
            Long organizationId,
            String employeeCode
    );

    boolean
    existsByOrganizationIdAndUserIdAndDeletedAtIsNull(
            Long organizationId,
            Long userId
    );
}
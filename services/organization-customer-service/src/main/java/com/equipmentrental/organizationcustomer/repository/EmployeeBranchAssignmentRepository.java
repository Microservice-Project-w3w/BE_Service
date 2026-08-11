package com.equipmentrental.organizationcustomer.repository;

import com.equipmentrental.organizationcustomer.entity.EmployeeBranchAssignment;
import com.equipmentrental.organizationcustomer.enums.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeBranchAssignmentRepository
        extends JpaRepository<EmployeeBranchAssignment, Long> {

    Optional<EmployeeBranchAssignment>
    findByIdAndOrganizationId(
            Long id,
            Long organizationId
    );


    List<EmployeeBranchAssignment>
    findAllByOrganizationIdOrderByIdDesc(
            Long organizationId
    );


    List<EmployeeBranchAssignment>
    findAllByOrganizationIdAndEmployeeIdOrderByIdDesc(
            Long organizationId,
            Long employeeId
    );


    List<EmployeeBranchAssignment>
    findAllByOrganizationIdAndEmployeeIdAndStatus(
            Long organizationId,
            Long employeeId,
            AssignmentStatus status
    );


    boolean
    existsByOrganizationIdAndEmployeeIdAndBranchId(
            Long organizationId,
            Long employeeId,
            Long branchId
    );
}
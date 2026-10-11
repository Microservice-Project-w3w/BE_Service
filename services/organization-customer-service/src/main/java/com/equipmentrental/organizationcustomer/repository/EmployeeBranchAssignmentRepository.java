package com.equipmentrental.organizationcustomer.repository;

import com.equipmentrental.organizationcustomer.entity.EmployeeBranchAssignment;
import com.equipmentrental.organizationcustomer.enums.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeBranchAssignmentRepository
        extends JpaRepository<EmployeeBranchAssignment, Long> {

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select a from EmployeeBranchAssignment a where a.organizationId = :organizationId and a.employeeId = :employeeId order by a.id desc")
    List<EmployeeBranchAssignment> findForTransfer(
            @org.springframework.data.repository.query.Param("organizationId") Long organizationId,
            @org.springframework.data.repository.query.Param("employeeId") Long employeeId);

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

package com.equipmentrental.rental.repository;

import com.equipmentrental.rental.entity.RentalRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalRequestRepository extends JpaRepository<RentalRequest, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select e from RentalRequest e where e.id = :id")
    java.util.Optional<RentalRequest> findForUpdate(@org.springframework.data.repository.query.Param("id") Long id);

    List<RentalRequest> findByOrganizationIdAndBranchId(Long organizationId, Long branchId);

    List<RentalRequest> findByOrganizationIdAndBranchIdAndCustomerId(
            Long organizationId, Long branchId, Long customerId);
}

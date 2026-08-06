package com.equipmentrental.rental.repository;

import com.equipmentrental.rental.entity.RentalRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RentalRequestRepository extends JpaRepository<RentalRequest, Long> {
    List<RentalRequest> findByOrganizationIdAndBranchId(Long organizationId, Long branchId);
}

package com.equipmentrental.rental.repository;

import com.equipmentrental.rental.entity.RentalRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalRequestRepository extends JpaRepository<RentalRequest, Long> {
    List<RentalRequest> findByOrganizationIdAndBranchId(Long organizationId, Long branchId);
}

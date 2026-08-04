package com.equipmentrental.rental.repository;

import com.equipmentrental.rental.entity.RentalContract;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalContractRepository extends JpaRepository<RentalContract, Long> {
    List<RentalContract> findByOrganizationIdAndBranchId(Long organizationId, Long branchId);
    boolean existsByRentalOrderId(Long rentalOrderId);
}

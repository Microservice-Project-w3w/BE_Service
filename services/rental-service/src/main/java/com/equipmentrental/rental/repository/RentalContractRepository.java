package com.equipmentrental.rental.repository;

import com.equipmentrental.rental.entity.RentalContract;
import com.equipmentrental.rental.entity.ContractStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalContractRepository extends JpaRepository<RentalContract, Long> {
    List<RentalContract> findByOrganizationIdAndBranchId(Long organizationId, Long branchId);

    List<RentalContract> findByOrganizationIdAndBranchIdAndCustomerId(
            Long organizationId, Long branchId, Long customerId);

    boolean existsByRentalOrderId(Long rentalOrderId);
    boolean existsByRentalOrderIdAndStatusNotIn(Long rentalOrderId, List<ContractStatus> statuses);
}

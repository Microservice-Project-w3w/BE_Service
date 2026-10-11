package com.equipmentrental.rental.repository;

import com.equipmentrental.rental.entity.RentalContract;
import com.equipmentrental.rental.entity.ContractStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalContractRepository extends JpaRepository<RentalContract, Long> {
    java.util.Optional<RentalContract> findByRentalOrderId(Long rentalOrderId);
    @org.springframework.data.jpa.repository.Query("select contract.rentalOrderId from RentalContract contract where contract.id = :id")
    java.util.Optional<Long> findRentalOrderId(@org.springframework.data.repository.query.Param("id") Long id);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select e from RentalContract e where e.id = :id")
    java.util.Optional<RentalContract> findForUpdate(@org.springframework.data.repository.query.Param("id") Long id);

    List<RentalContract> findByOrganizationIdAndBranchId(Long organizationId, Long branchId);

    List<RentalContract> findByOrganizationIdAndBranchIdAndCustomerId(
            Long organizationId, Long branchId, Long customerId);

    boolean existsByRentalOrderId(Long rentalOrderId);
    boolean existsByRentalOrderIdAndStatusNotIn(Long rentalOrderId, List<ContractStatus> statuses);
}

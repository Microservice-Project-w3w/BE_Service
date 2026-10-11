package com.equipmentrental.rental.repository;

import com.equipmentrental.rental.entity.Quotation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuotationRepository extends JpaRepository<Quotation, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select e from Quotation e where e.id = :id")
    java.util.Optional<Quotation> findForUpdate(@org.springframework.data.repository.query.Param("id") Long id);

    List<Quotation> findByOrganizationIdAndBranchId(Long organizationId, Long branchId);

    boolean existsByRentalRequestIdAndStatusNotIn(Long requestId,
            List<com.equipmentrental.rental.entity.QuotationStatus> statuses);

    List<Quotation> findByOrganizationIdAndBranchIdAndCustomerId(
            Long organizationId, Long branchId, Long customerId);
}

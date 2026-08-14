package com.equipmentrental.rental.repository;

import com.equipmentrental.rental.entity.Quotation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuotationRepository extends JpaRepository<Quotation, Long> {
    List<Quotation> findByOrganizationIdAndBranchId(Long organizationId, Long branchId);

    List<Quotation> findByOrganizationIdAndBranchIdAndCustomerId(
            Long organizationId, Long branchId, Long customerId);
}

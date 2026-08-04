package com.equipmentrental.rental.repository;

import com.equipmentrental.rental.entity.Quotation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuotationRepository extends JpaRepository<Quotation, Long> {
    List<Quotation> findByOrganizationIdAndBranchId(Long organizationId, Long branchId);
}

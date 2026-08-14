package com.equipmentrental.rental.repository;

import com.equipmentrental.rental.entity.DiscountCode;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscountCodeRepository extends JpaRepository<DiscountCode, Long> {
    List<DiscountCode> findByOrganizationIdAndBranchId(Long organizationId, Long branchId);

    Optional<DiscountCode> findByOrganizationIdAndBranchIdAndCodeIgnoreCase(
            Long organizationId, Long branchId, String code);
}

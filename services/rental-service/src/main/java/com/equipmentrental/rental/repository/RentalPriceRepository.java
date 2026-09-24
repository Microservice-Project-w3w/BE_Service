package com.equipmentrental.rental.repository;

import com.equipmentrental.rental.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalPriceRepository extends JpaRepository<RentalPrice, Long> {
    List<RentalPrice> findByOrganizationIdAndBranchId(Long organizationId, Long branchId);

    List<RentalPrice> findByEquipmentTypeIdAndActiveTrue(Long equipmentTypeId);

    Optional<RentalPrice> findFirstByOrganizationIdAndBranchIdAndEquipmentTypeIdAndRentalUnitAndActiveTrueOrderByValidFromDesc(
            Long organizationId, Long branchId, Long equipmentTypeId, RentalUnit unit);
}

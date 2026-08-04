package com.equipmentrental.rental.repository;

import com.equipmentrental.rental.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface RentalPriceRepository extends JpaRepository<RentalPrice, Long> {
    List<RentalPrice> findByEquipmentTypeIdAndActiveTrue(Long equipmentTypeId);

    Optional<RentalPrice> findFirstByEquipmentTypeIdAndRentalUnitAndActiveTrueOrderByValidFromDesc(Long equipmentTypeId,
                                                                                                   RentalUnit unit);
}

package com.equipmentrental.rental.repository;

import com.equipmentrental.rental.entity.RentalPrice;
import com.equipmentrental.rental.entity.RentalUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RentalPriceRepository extends JpaRepository<RentalPrice, Long> {

    List<RentalPrice> findAllByOrderByCreatedAtDesc();

    List<RentalPrice> findByActiveTrueOrderByCreatedAtDesc();

    List<RentalPrice> findByEquipmentTypeIdOrderByCreatedAtDesc(
            Long equipmentTypeId
    );

    Optional<RentalPrice> findByEquipmentTypeIdAndRentalUnit(
            Long equipmentTypeId,
            RentalUnit rentalUnit
    );
}
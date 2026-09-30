package com.equipmentrental.inventory.repository;

import com.equipmentrental.inventory.entity.EquipmentReservation;
import com.equipmentrental.inventory.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface EquipmentReservationRepository
        extends JpaRepository<EquipmentReservation, Long> {

    List<EquipmentReservation> findByRentalOrderId(
            Long rentalOrderId
    );

    List<EquipmentReservation> findByStatus(
            ReservationStatus status
    );

    List<EquipmentReservation> findByRentalOrderIdAndStatus(
            Long rentalOrderId,
            ReservationStatus status
    );
    Optional<EquipmentReservation>
    findByOrganizationIdAndRequestReference(
            Long organizationId,
            String requestReference
    );
    boolean existsByOrganizationIdAndRequestReference(
            Long organizationId,
            String requestReference
    );
}
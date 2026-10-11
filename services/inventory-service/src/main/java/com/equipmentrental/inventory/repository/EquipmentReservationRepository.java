package com.equipmentrental.inventory.repository;

import com.equipmentrental.inventory.entity.EquipmentReservation;
import com.equipmentrental.inventory.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
@Repository
public interface EquipmentReservationRepository
        extends JpaRepository<EquipmentReservation, Long> {
    @org.springframework.data.jpa.repository.Query("select r.id from EquipmentReservation r where r.organizationId=:org and r.requestReference=:reference")
    Optional<Long> findDemoIdByReference(@org.springframework.data.repository.query.Param("org") Long org,
            @org.springframework.data.repository.query.Param("reference") String reference);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select r from EquipmentReservation r where r.id=:id")
    Optional<EquipmentReservation> findForDemoUpdate(@org.springframework.data.repository.query.Param("id") Long id);

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

    List<EquipmentReservation> findByStatusAndExpiresAtBefore(ReservationStatus status, LocalDateTime expiresAt);
}

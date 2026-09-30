package com.equipmentrental.inventory.repository;

import com.equipmentrental.inventory.entity.EquipmentReservation;
import com.equipmentrental.inventory.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface EquipmentReservationRepository
        extends JpaRepository<EquipmentReservation, Long> {

    List<EquipmentReservation> findByRentalId(Long rentalId);

    @Query("""
            select case when count(r) > 0 then true else false end
            from EquipmentReservation r
            where r.equipment.id = :equipmentId
              and r.status in :statuses
              and r.startAt < :endAt
              and r.endAt > :startAt
            """)
    boolean existsOverlappingReservation(
            @Param("equipmentId") Long equipmentId,
            @Param("statuses") Collection<ReservationStatus> statuses,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );
}
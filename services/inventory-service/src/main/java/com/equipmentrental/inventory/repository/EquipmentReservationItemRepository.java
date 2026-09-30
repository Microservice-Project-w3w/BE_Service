package com.equipmentrental.inventory.repository;

import com.equipmentrental.inventory.entity.EquipmentReservationItem;
import com.equipmentrental.inventory.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EquipmentReservationItemRepository
        extends JpaRepository<EquipmentReservationItem, Long> {

    /*
     * Dùng khi cần lấy toàn bộ item của một reservation.
     *
     * Ví dụ:
     * reservation_id = 5
     * -> lấy toàn bộ equipment nằm trong reservation 5.
     */
    List<EquipmentReservationItem> findByReservationId(
            Long reservationId
    );

    /*
     * Kiểm tra các EquipmentReservationItem
     * đang chặn khoảng thời gian được yêu cầu.
     *
     * Điều kiện overlap:
     *
     * existing.startAt < requestedEnd
     * AND
     * existing.endAt > requestedStart
     *
     * Các reservation HELD đã hết expiresAt
     * sẽ không còn block availability.
     */
    @Query("""
        SELECT i
        FROM EquipmentReservationItem i
        JOIN EquipmentReservation r
            ON r.id = i.reservationId
        WHERE r.organizationId = :organizationId
          AND r.branchId = :branchId
          AND r.status IN :statuses
          AND i.equipmentTypeId = :equipmentTypeId
          AND i.equipmentId IN :equipmentIds
          AND i.startAt < :requestedEnd
          AND i.endAt > :requestedStart
          AND (
                r.status <> com.equipmentrental.inventory.enums.ReservationStatus.HELD
                OR r.expiresAt IS NULL
                OR r.expiresAt > :now
              )
        """)
    List<EquipmentReservationItem> findBlockingItems(

            @Param("organizationId")
            Long organizationId,

            @Param("branchId")
            Long branchId,

            @Param("equipmentTypeId")
            Long equipmentTypeId,

            @Param("equipmentIds")
            List<Long> equipmentIds,

            @Param("statuses")
            List<ReservationStatus> statuses,

            @Param("requestedStart")
            LocalDateTime requestedStart,

            @Param("requestedEnd")
            LocalDateTime requestedEnd,

            @Param("now")
            LocalDateTime now
    );
}
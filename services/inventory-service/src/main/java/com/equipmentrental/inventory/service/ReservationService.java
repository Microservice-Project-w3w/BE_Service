package com.equipmentrental.inventory.service;

import com.equipmentrental.inventory.dto.ReservationResponse;
import com.equipmentrental.inventory.dto.ReserveEquipmentRequest;
import com.equipmentrental.inventory.entity.Equipment;
import com.equipmentrental.inventory.entity.EquipmentReservation;
import com.equipmentrental.inventory.enums.EquipmentStatus;
import com.equipmentrental.inventory.enums.ReservationStatus;
import com.equipmentrental.inventory.exception.ResourceNotFoundException;
import com.equipmentrental.inventory.repository.EquipmentRepository;
import com.equipmentrental.inventory.repository.EquipmentReservationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationService {

    private static final List<ReservationStatus> ACTIVE_STATUSES =
            List.of(
                    ReservationStatus.HELD,
                    ReservationStatus.CONFIRMED
            );

    private final EquipmentRepository equipmentRepository;
    private final EquipmentReservationRepository reservationRepository;

    public ReservationService(
            EquipmentRepository equipmentRepository,
            EquipmentReservationRepository reservationRepository
    ) {
        this.equipmentRepository = equipmentRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public ReservationResponse reserve(
            ReserveEquipmentRequest request
    ) {
        validatePeriod(
                request.startAt(),
                request.endAt()
        );

        Equipment equipment = equipmentRepository
                .findByIdForUpdate(request.equipmentId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Equipment not found: "
                                        + request.equipmentId()
                        )
                );

        validateEquipmentCanBeReserved(equipment);

        boolean overlapping =
                reservationRepository.existsOverlappingReservation(
                        equipment.getId(),
                        ACTIVE_STATUSES,
                        request.startAt(),
                        request.endAt()
                );

        if (overlapping) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Equipment is already reserved for this period"
            );
        }

        EquipmentReservation reservation =
                new EquipmentReservation();

        reservation.setEquipment(equipment);
        reservation.setRentalId(request.rentalId());
        reservation.setStartAt(request.startAt());
        reservation.setEndAt(request.endAt());
        reservation.setStatus(ReservationStatus.HELD);

        // HELD tạm thời 15 phút.
        reservation.setExpiresAt(
                LocalDateTime.now().plusMinutes(15)
        );

        EquipmentReservation saved =
                reservationRepository.save(reservation);

        return toResponse(saved);
    }

    @Transactional
    public ReservationResponse confirm(
            Long reservationId
    ) {
        EquipmentReservation reservation =
                reservationRepository
                        .findById(reservationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Reservation not found: "
                                                + reservationId
                                )
                        );

        if (reservation.getStatus()
                != ReservationStatus.HELD) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only HELD reservation can be confirmed"
            );
        }

        if (reservation.getExpiresAt() != null
                && reservation.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            reservation.setStatus(
                    ReservationStatus.EXPIRED
            );

            reservationRepository.save(reservation);

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Reservation hold has expired"
            );
        }

        reservation.setStatus(
                ReservationStatus.CONFIRMED
        );

        reservation.setExpiresAt(null);

        EquipmentReservation saved =
                reservationRepository.save(reservation);

        return toResponse(saved);
    }

    @Transactional
    public ReservationResponse release(
            Long reservationId
    ) {
        EquipmentReservation reservation =
                reservationRepository
                        .findById(reservationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Reservation not found: "
                                                + reservationId
                                )
                        );

        if (reservation.getStatus()
                == ReservationStatus.RELEASED) {

            return toResponse(reservation);
        }

        if (reservation.getStatus()
                == ReservationStatus.CANCELLED
                || reservation.getStatus()
                == ReservationStatus.EXPIRED) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Reservation is not active"
            );
        }

        reservation.setStatus(
                ReservationStatus.RELEASED
        );

        reservation.setExpiresAt(null);

        EquipmentReservation saved =
                reservationRepository.save(reservation);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ReservationResponse get(
            Long reservationId
    ) {
        EquipmentReservation reservation =
                reservationRepository
                        .findById(reservationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Reservation not found: "
                                                + reservationId
                                )
                        );

        return toResponse(reservation);
    }

    private void validatePeriod(
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        if (!startAt.isBefore(endAt)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "startAt must be before endAt"
            );
        }
    }

    private void validateEquipmentCanBeReserved(
            Equipment equipment
    ) {
        switch (equipment.getStatus()) {
            case CHECKED_OUT,
                 IN_TRANSIT,
                 INSPECTION,
                 MAINTENANCE,
                 LOST,
                 DAMAGED,
                 RETIRED ->
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Equipment cannot be reserved because status is "
                                    + equipment.getStatus()
                    );

            case AVAILABLE,
                 RESERVED -> {
                // Cho phép kiểm tra tiếp bằng khoảng thời gian reservation.
            }
        }
    }

    private ReservationResponse toResponse(
            EquipmentReservation reservation
    ) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getEquipment().getId(),
                reservation.getRentalId(),
                reservation.getStartAt(),
                reservation.getEndAt(),
                reservation.getExpiresAt(),
                reservation.getStatus()
        );
    }
}
package com.equipmentrental.inventory.service;

import com.equipmentrental.inventory.dto.response.EquipmentReservationResponse;
import com.equipmentrental.inventory.entity.EquipmentReservation;
import com.equipmentrental.inventory.enums.ReservationStatus;
import com.equipmentrental.inventory.exception.ResourceNotFoundException;
import com.equipmentrental.inventory.repository.EquipmentReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentReservationQueryService {

    private final EquipmentReservationRepository repository;

    @Transactional(readOnly = true)
    public List<EquipmentReservationResponse> findAll(
            Long rentalOrderId,
            ReservationStatus status
    ) {

        List<EquipmentReservation> reservations;

        if (rentalOrderId != null
                && status != null) {

            reservations =
                    repository
                            .findByRentalOrderIdAndStatus(
                                    rentalOrderId,
                                    status
                            );

        } else if (rentalOrderId != null) {

            reservations =
                    repository
                            .findByRentalOrderId(
                                    rentalOrderId
                            );

        } else if (status != null) {

            reservations =
                    repository
                            .findByStatus(
                                    status
                            );

        } else {

            reservations =
                    repository.findAll();
        }

        return reservations
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EquipmentReservationResponse findById(
            Long id
    ) {

        EquipmentReservation reservation =
                repository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Reservation not found: "
                                                + id
                                )
                        );

        return toResponse(reservation);
    }

    private EquipmentReservationResponse toResponse(
            EquipmentReservation reservation
    ) {

        return new EquipmentReservationResponse(

                reservation.getId(),

                reservation.getOrganizationId(),

                reservation.getBranchId(),

                reservation.getReservationCode(),

                reservation.getRequestReference(),

                reservation.getRentalOrderId(),

                reservation.getStartAt(),

                reservation.getEndAt(),

                reservation.getExpiresAt(),

                reservation.getStatus(),

                reservation.getCreatedBy(),

                reservation.getCreatedAt(),

                reservation.getUpdatedAt(),

                reservation.getVersion()
        );
    }
}
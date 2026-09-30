package com.equipmentrental.inventory.service;

import com.equipmentrental.inventory.dto.AvailabilityRequest;
import com.equipmentrental.inventory.dto.AvailabilityResponse;
import com.equipmentrental.inventory.entity.Equipment;
import com.equipmentrental.inventory.enums.EquipmentStatus;
import com.equipmentrental.inventory.enums.ReservationStatus;
import com.equipmentrental.inventory.exception.ResourceNotFoundException;
import com.equipmentrental.inventory.repository.EquipmentRepository;
import com.equipmentrental.inventory.repository.EquipmentReservationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AvailabilityService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentReservationRepository reservationRepository;

    public AvailabilityService(
            EquipmentRepository equipmentRepository,
            EquipmentReservationRepository reservationRepository
    ) {
        this.equipmentRepository = equipmentRepository;
        this.reservationRepository = reservationRepository;
    }

    public AvailabilityResponse checkAvailability(AvailabilityRequest request) {

        if (!request.startAt().isBefore(request.endAt())) {
            throw new IllegalArgumentException(
                    "startAt must be before endAt"
            );
        }

        Equipment equipment = equipmentRepository
                .findById(request.equipmentId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Equipment not found: " + request.equipmentId()
                        )
                );

        if (equipment.getStatus() != EquipmentStatus.AVAILABLE
            && equipment.getStatus() != EquipmentStatus.RESERVED) {
            return new AvailabilityResponse(
                    equipment.getId(),
                    false,
                    equipment.getStatus(),
                    "Equipment status is not AVAILABLE"
            );
        }

        boolean overlapping = reservationRepository
                .existsOverlappingReservation(
                        equipment.getId(),
                        List.of(
                                ReservationStatus.HELD,
                                ReservationStatus.CONFIRMED
                        ),
                        request.startAt(),
                        request.endAt()
                );

        if (overlapping) {
            return new AvailabilityResponse(
                    equipment.getId(),
                    false,
                    equipment.getStatus(),
                    "Equipment already has an active reservation"
            );
        }

        return new AvailabilityResponse(
                equipment.getId(),
                true,
                equipment.getStatus(),
                "Equipment is available"
        );
    }
}

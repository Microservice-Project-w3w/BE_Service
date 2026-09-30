package com.equipmentrental.inventory.service;

import com.equipmentrental.inventory.dto.request.ConfirmInternalReservationRequest;
import com.equipmentrental.inventory.dto.request.CreateInternalReservationItemRequest;
import com.equipmentrental.inventory.dto.request.CreateInternalReservationRequest;
import com.equipmentrental.inventory.dto.response.EquipmentReservationResponse;
import com.equipmentrental.inventory.entity.Equipment;
import com.equipmentrental.inventory.entity.EquipmentModel;
import com.equipmentrental.inventory.entity.EquipmentReservation;
import com.equipmentrental.inventory.entity.EquipmentReservationItem;
import com.equipmentrental.inventory.enums.EquipmentStatus;
import com.equipmentrental.inventory.enums.ReservationStatus;
import com.equipmentrental.inventory.exception.ResourceNotFoundException;
import com.equipmentrental.inventory.repository.EquipmentModelRepository;
import com.equipmentrental.inventory.repository.EquipmentRepository;
import com.equipmentrental.inventory.repository.EquipmentReservationItemRepository;
import com.equipmentrental.inventory.repository.EquipmentReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.equipmentrental.inventory.dto.request.ReleaseInternalReservationRequest;

@Service
@RequiredArgsConstructor
public class InternalReservationService {

    private final EquipmentReservationRepository reservationRepository;
    private final EquipmentReservationItemRepository itemRepository;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentModelRepository equipmentModelRepository;

    // =====================================================
    // CREATE RESERVATION
    // =====================================================

    @Transactional
    public EquipmentReservationResponse create(
            CreateInternalReservationRequest request
    ) {

        validate(request);

        String requestReference =
                request.requestReference()
                        .trim();

        /*
         * =====================================================
         * IDEMPOTENCY
         * =====================================================
         *
         * Cùng organizationId + requestReference
         * thì không tạo reservation mới.
         */
        EquipmentReservation existing =
                reservationRepository
                        .findByOrganizationIdAndRequestReference(
                                request.organizationId(),
                                requestReference
                        )
                        .orElse(null);

        if (existing != null) {
            return toResponse(existing);
        }

        /*
         * =====================================================
         * VALIDATE TOÀN BỘ EQUIPMENT TRƯỚC
         * =====================================================
         */
        for (CreateInternalReservationItemRequest item
                : request.items()) {

            Equipment equipment =
                    getEquipment(
                            item.equipmentId(),
                            request.organizationId()
                    );

            /*
             * Equipment phải thuộc đúng branch.
             */
            if (!equipment.getBranchId()
                    .equals(request.branchId())) {

                throw new IllegalArgumentException(
                        "Equipment "
                                + equipment.getId()
                                + " does not belong to branch "
                                + request.branchId()
                );
            }

            /*
             * Chỉ Equipment AVAILABLE mới được reserve.
             */
            if (equipment.getStatus()
                    != EquipmentStatus.AVAILABLE) {

                throw new IllegalStateException(
                        "Equipment "
                                + equipment.getId()
                                + " is not AVAILABLE"
                );
            }

            /*
             * Equipment phải đang nằm trong kho.
             */
            if (equipment.getWarehouseId() == null) {

                throw new IllegalStateException(
                        "Equipment "
                                + equipment.getId()
                                + " is not currently in warehouse"
                );
            }

            /*
             * Kiểm tra model tồn tại.
             */
            getEquipmentModel(
                    equipment.getModelId()
            );
        }

        /*
         * =====================================================
         * CREATE RESERVATION HEADER
         * =====================================================
         */

        EquipmentReservation reservation =
                EquipmentReservation.builder()

                        .organizationId(
                                request.organizationId()
                        )

                        .branchId(
                                request.branchId()
                        )

                        .reservationCode(
                                generateReservationCode(
                                        request.rentalOrderId(),
                                        requestReference
                                )
                        )

                        .requestReference(
                                requestReference
                        )

                        .rentalOrderId(
                                request.rentalOrderId()
                        )

                        .startAt(
                                request.startAt()
                        )

                        .endAt(
                                request.endAt()
                        )

                        .status(
                                ReservationStatus.HELD
                        )

                        .build();

        reservation =
                reservationRepository.save(
                        reservation
                );

        Long reservationId =
                reservation.getId();

        /*
         * =====================================================
         * CREATE RESERVATION ITEMS
         * =====================================================
         */

        for (CreateInternalReservationItemRequest requestItem
                : request.items()) {

            Equipment equipment =
                    getEquipment(
                            requestItem.equipmentId(),
                            request.organizationId()
                    );

            EquipmentModel model =
                    getEquipmentModel(
                            equipment.getModelId()
                    );

            EquipmentReservationItem item =
                    EquipmentReservationItem.builder()

                            .reservationId(
                                    reservationId
                            )

                            .equipmentTypeId(
                                    model.getEquipmentTypeId()
                            )

                            .equipmentId(
                                    equipment.getId()
                            )

                            .startAt(
                                    request.startAt()
                            )

                            .endAt(
                                    request.endAt()
                            )

                            .build();

            itemRepository.save(item);
        }

        return toResponse(reservation);
    }

    // =====================================================
    // CONFIRM RESERVATION
    // =====================================================

    @Transactional
    public EquipmentReservationResponse confirm(
            Long id,
            ConfirmInternalReservationRequest request
    ) {

        EquipmentReservation reservation =
                reservationRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Reservation not found: "
                                                + id
                                )
                        );

        /*
         * Workflow:
         *
         * HELD
         *   ↓
         * CONFIRMED
         *
         * Các trạng thái khác không được confirm.
         */
        if (reservation.getStatus()
                != ReservationStatus.HELD) {

            throw new IllegalStateException(
                    "Only HELD reservation can be confirmed"
            );
        }

        /*
         * Nếu Rental Service gửi rentalOrderId
         * thì phải khớp với reservation đã tạo.
         */
        if (request != null
                && request.rentalOrderId() != null) {

            if (reservation.getRentalOrderId() == null
                    || !reservation.getRentalOrderId()
                    .equals(request.rentalOrderId())) {

                throw new IllegalArgumentException(
                        "rentalOrderId does not match reservation"
                );
            }
        }

        /*
         * Kiểm tra reservation phải có item.
         */
        if (itemRepository
                .findByReservationId(
                        reservation.getId()
                )
                .isEmpty()) {

            throw new IllegalStateException(
                    "Reservation has no items"
            );
        }

        /*
         * Chuyển trạng thái.
         *
         * Entity hiện tại chưa có:
         * confirmedBy
         * confirmedAt
         *
         * nên actorUserId chưa được persist.
         */
        reservation.setStatus(
                ReservationStatus.CONFIRMED
        );

        reservation =
                reservationRepository.save(
                        reservation
                );

        return toResponse(reservation);
    }

    // =====================================================
    // VALIDATE CREATE REQUEST
    // =====================================================

    private void validate(
            CreateInternalReservationRequest request
    ) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "request is required"
            );
        }

        if (request.requestReference() == null
                || request.requestReference().isBlank()) {

            throw new IllegalArgumentException(
                    "requestReference is required"
            );
        }

        if (request.organizationId() == null) {

            throw new IllegalArgumentException(
                    "organizationId is required"
            );
        }

        if (request.branchId() == null) {

            throw new IllegalArgumentException(
                    "branchId is required"
            );
        }

        if (request.rentalOrderId() == null) {

            throw new IllegalArgumentException(
                    "rentalOrderId is required"
            );
        }

        if (request.startAt() == null
                || request.endAt() == null) {

            throw new IllegalArgumentException(
                    "startAt and endAt are required"
            );
        }

        if (!request.endAt()
                .isAfter(request.startAt())) {

            throw new IllegalArgumentException(
                    "endAt must be after startAt"
            );
        }

        if (request.items() == null
                || request.items().isEmpty()) {

            throw new IllegalArgumentException(
                    "items must not be empty"
            );
        }

        for (CreateInternalReservationItemRequest item
                : request.items()) {

            if (item == null
                    || item.equipmentId() == null) {

                throw new IllegalArgumentException(
                        "equipmentId is required for every item"
                );
            }
        }
    }

    // =====================================================
    // GET EQUIPMENT
    // =====================================================

    private Equipment getEquipment(
            Long equipmentId,
            Long organizationId
    ) {

        return equipmentRepository
                .findByIdAndOrganizationId(
                        equipmentId,
                        organizationId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Equipment not found: "
                                        + equipmentId
                        )
                );
    }

    // =====================================================
    // GET EQUIPMENT MODEL
    // =====================================================

    private EquipmentModel getEquipmentModel(
            Long modelId
    ) {

        return equipmentModelRepository
                .findById(modelId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Equipment model not found: "
                                        + modelId
                        )
                );
    }

    // =====================================================
    // GENERATE RESERVATION CODE
    // =====================================================

    private String generateReservationCode(
            Long rentalOrderId,
            String requestReference
    ) {

        return "RSV-"
                + rentalOrderId
                + "-"
                + Math.abs(
                requestReference.hashCode()
        );
    }

    // =====================================================
    // RESPONSE
    // =====================================================

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
    // =====================================================
// RELEASE RESERVATION
// =====================================================

    @Transactional
    public EquipmentReservationResponse release(
            Long id,
            ReleaseInternalReservationRequest request
    ) {

        EquipmentReservation reservation =
                reservationRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Reservation not found: " + id
                                )
                        );

        /*
         * Chỉ HELD hoặc CONFIRMED mới được RELEASE.
         */
        if (reservation.getStatus() != ReservationStatus.HELD
                && reservation.getStatus() != ReservationStatus.CONFIRMED) {

            throw new IllegalStateException(
                    "Only HELD or CONFIRMED reservation can be released"
            );
        }

        /*
         * Validate request.
         */
        if (request == null) {
            throw new IllegalArgumentException(
                    "request is required"
            );
        }

        if (request.reason() == null
                || request.reason().isBlank()) {

            throw new IllegalArgumentException(
                    "reason is required"
            );
        }

        /*
         * Đảm bảo reservation có item.
         */
        if (itemRepository
                .findByReservationId(
                        reservation.getId()
                )
                .isEmpty()) {

            throw new IllegalStateException(
                    "Reservation has no items"
            );
        }

        /*
         * Chuyển trạng thái.
         */
        reservation.setStatus(
                ReservationStatus.RELEASED
        );

        /*
         * Entity hiện tại chưa có:
         * releasedBy
         * releasedAt
         * releaseReason
         *
         * nên actorUserId và reason hiện mới dùng
         * để validate request, chưa persist.
         */

        reservation =
                reservationRepository.save(
                        reservation
                );

        return toResponse(reservation);
    }
}
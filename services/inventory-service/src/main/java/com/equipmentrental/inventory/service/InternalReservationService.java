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
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class InternalReservationService {

    private final EquipmentReservationRepository reservationRepository;
    private final EquipmentReservationItemRepository itemRepository;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentModelRepository equipmentModelRepository;
    @org.springframework.beans.factory.annotation.Value("${RENTAL_SERVICE_URL:http://localhost:8084}")
    private String rentalBaseUrl;

    @Transactional
    public EquipmentReservationResponse extend(Long id, LocalDateTime newEndAt) {
        EquipmentReservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + id));
        if (reservation.getStatus() != ReservationStatus.CONFIRMED)
            throw new IllegalStateException("Chỉ gia hạn được giữ chỗ đã xác nhận");
        // A customer may only extend their own order, not any reservation in their branch.
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken jwt
                && jwt.getToken().getClaimAsStringList("roles") != null
                && jwt.getToken().getClaimAsStringList("roles").contains("CUSTOMER")) {
            org.springframework.web.client.RestClient.create(rentalBaseUrl).get()
                    .uri("/api/v1/rental-orders/" + reservation.getRentalOrderId())
                    .headers(headers -> headers.setBearerAuth(jwt.getToken().getTokenValue())).retrieve().toBodilessEntity();
        }
        if (newEndAt == null || newEndAt.isBefore(reservation.getEndAt()))
            throw new IllegalArgumentException("Ngày kết thúc gia hạn không được trước ngày hiện tại");
        if (newEndAt.equals(reservation.getEndAt())) return toResponse(reservation); // Safe retry after a remote commit.
        List<EquipmentReservationItem> items = itemRepository.findByReservationId(id);
        if (items.isEmpty()) throw new IllegalStateException("Giữ chỗ không có thiết bị");
        for (var equipmentId : items.stream().map(EquipmentReservationItem::getEquipmentId).sorted().toList())
            getEquipment(equipmentId, reservation.getOrganizationId());
        for (var item : items) {
            var blocking = itemRepository.findBlockingItems(reservation.getOrganizationId(), reservation.getBranchId(),
                    item.getEquipmentTypeId(), List.of(item.getEquipmentId()), List.of(ReservationStatus.HELD, ReservationStatus.CONFIRMED),
                    reservation.getEndAt(), newEndAt, LocalDateTime.now());
            if (blocking.stream().anyMatch(other -> !id.equals(other.getReservationId())))
                throw new IllegalStateException("Thiết bị đã có giữ chỗ khác trong thời gian gia hạn");
        }
        reservation.setEndAt(newEndAt); reservationRepository.save(reservation);
        for (var item : items) item.setEndAt(newEndAt);
        itemRepository.saveAll(items);
        return toResponse(reservation);
    }

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
        for (var equipmentId : request.items().stream().map(CreateInternalReservationItemRequest::equipmentId).sorted().toList())
            getEquipment(equipmentId, request.organizationId());
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
            EquipmentModel model = getEquipmentModel(
                    equipment.getModelId()
            );

            if (!itemRepository.findBlockingItems(request.organizationId(), request.branchId(),
                    model.getEquipmentTypeId(), List.of(equipment.getId()),
                    List.of(ReservationStatus.HELD, ReservationStatus.CONFIRMED), request.startAt(), request.endAt(),
                    LocalDateTime.now()).isEmpty()) {
                throw new IllegalStateException("Equipment " + equipment.getId()
                        + " đã được giữ chỗ trong khoảng thời gian yêu cầu");
            }
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

                        .expiresAt(
                                request.expiresAt()
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

        if (reservation.getExpiresAt() != null && !reservation.getExpiresAt().isAfter(LocalDateTime.now())) {
            reservation.setStatus(ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);
            throw new IllegalStateException("Reservation đã hết hạn");
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

        if (request.expiresAt() == null
                || !request.expiresAt().isAfter(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "expiresAt must be in the future"
            );
        }

        if (request.items() == null
                || request.items().isEmpty()) {

            throw new IllegalArgumentException(
                    "items must not be empty"
            );
        }

        Set<Long> equipmentIds = new HashSet<>();
        for (CreateInternalReservationItemRequest item
                : request.items()) {

            if (item == null
                    || item.equipmentId() == null) {

                throw new IllegalArgumentException(
                        "equipmentId is required for every item"
                );
            }
            if (!equipmentIds.add(item.equipmentId())) {
                throw new IllegalArgumentException("equipmentId bị lặp trong reservation: " + item.equipmentId());
            }
        }
    }

    @Transactional
    public int expireOverdueReservations() {
        List<EquipmentReservation> expired = reservationRepository
                .findByStatusAndExpiresAtBefore(ReservationStatus.HELD, LocalDateTime.now());
        expired.forEach(reservation -> reservation.setStatus(ReservationStatus.EXPIRED));
        return expired.size();
    }

    // =====================================================
    // GET EQUIPMENT
    // =====================================================

    private Equipment getEquipment(
            Long equipmentId,
            Long organizationId
    ) {

        return equipmentRepository
                .lockForReservation(
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

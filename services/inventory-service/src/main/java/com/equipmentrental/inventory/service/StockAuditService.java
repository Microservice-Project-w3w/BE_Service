package com.equipmentrental.inventory.service;

import com.equipmentrental.inventory.dto.request.*;
import com.equipmentrental.inventory.dto.response.*;
import com.equipmentrental.inventory.entity.*;
import com.equipmentrental.inventory.enums.*;
import com.equipmentrental.inventory.exception.ResourceNotFoundException;
import com.equipmentrental.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockAuditService {

    private final StockAuditRepository auditRepository;
    private final StockAuditItemRepository itemRepository;

    private final WarehouseRepository warehouseRepository;
    private final EquipmentRepository equipmentRepository;

    // =====================================================
    // CREATE
    // =====================================================

    @Transactional
    public StockAuditResponse create(
            CreateStockAuditRequest request
    ) {

        Warehouse warehouse =
                warehouseRepository
                        .findById(request.warehouseId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Warehouse not found: "
                                                + request.warehouseId()
                                )
                        );

        if (!warehouse.getOrganizationId()
                .equals(request.organizationId())) {

            throw new IllegalArgumentException(
                    "Warehouse does not belong to organization"
            );
        }

        if (!warehouse.getBranchId()
                .equals(request.branchId())) {

            throw new IllegalArgumentException(
                    "Warehouse does not belong to branch"
            );
        }

        String code =
                request.auditCode()
                        .trim()
                        .toUpperCase();

        if (auditRepository
                .existsByOrganizationIdAndAuditCode(
                        request.organizationId(),
                        code
                )) {

            throw new IllegalArgumentException(
                    "Audit code already exists"
            );
        }

        StockAudit audit =
                StockAudit.builder()
                        .organizationId(
                                request.organizationId()
                        )
                        .branchId(
                                request.branchId()
                        )
                        .warehouseId(
                                request.warehouseId()
                        )
                        .auditCode(code)
                        .note(
                                normalize(request.note())
                        )
                        .status(
                                StockAuditStatus.DRAFT
                        )
                        .createdBy(
                                request.createdBy()
                        )
                        .build();

        audit =
                auditRepository.save(audit);

        return toResponse(audit);
    }

    // =====================================================
    // GET ALL
    // =====================================================

    @Transactional(readOnly = true)
    public List<StockAuditResponse> findAll(
            Long organizationId,
            Long branchId,
            Long warehouseId
    ) {

        List<StockAudit> result;

        if (organizationId != null
                && warehouseId != null) {

            result =
                    auditRepository
                            .findByOrganizationIdAndWarehouseId(
                                    organizationId,
                                    warehouseId
                            );

        } else if (organizationId != null
                && branchId != null) {

            result =
                    auditRepository
                            .findByOrganizationIdAndBranchId(
                                    organizationId,
                                    branchId
                            );

        } else if (organizationId != null) {

            result =
                    auditRepository
                            .findByOrganizationId(
                                    organizationId
                            );

        } else {

            result =
                    auditRepository.findAll();
        }

        return result
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =====================================================
    // GET BY ID
    // =====================================================

    @Transactional(readOnly = true)
    public StockAuditResponse findById(
            Long id
    ) {

        return toResponse(
                getAudit(id)
        );
    }

    // =====================================================
    // START
    // =====================================================

    @Transactional
    public StockAuditResponse start(
            Long id,
            StartStockAuditRequest request
    ) {

        StockAudit audit =
                getAudit(id);

        if (audit.getStatus()
                != StockAuditStatus.DRAFT) {

            throw new IllegalStateException(
                    "Only DRAFT audit can be started"
            );
        }

        /*
         * Snapshot tất cả thiết bị hiện đang được
         * hệ thống ghi nhận thuộc kho này.
         */
        List<Equipment> equipmentList =
                equipmentRepository
                        .findByOrganizationIdAndBranchIdAndWarehouseId(
                                audit.getOrganizationId(),
                                audit.getBranchId(),
                                audit.getWarehouseId()
                        );

        for (Equipment equipment : equipmentList) {

            StockAuditItem item =
                    StockAuditItem.builder()
                            .stockAuditId(
                                    audit.getId()
                            )
                            .equipmentId(
                                    equipment.getId()
                            )
                            .expectedWarehouseId(
                                    audit.getWarehouseId()
                            )
                            .expectedLocationId(
                                    equipment.getWarehouseLocationId()
                            )
                            .build();

            itemRepository.save(item);
        }

        audit.setStatus(
                StockAuditStatus.IN_PROGRESS
        );

        audit.setStartedBy(
                request == null
                        ? null
                        : request.startedBy()
        );

        audit.setStartedAt(
                LocalDateTime.now()
        );

        return toResponse(
                auditRepository.save(audit)
        );
    }

    // =====================================================
    // RECORD RESULT
    // =====================================================

    @Transactional
    public StockAuditResponse recordItem(
            Long auditId,
            RecordStockAuditItemRequest request
    ) {

        StockAudit audit =
                getAudit(auditId);

        if (audit.getStatus()
                != StockAuditStatus.IN_PROGRESS) {

            throw new IllegalStateException(
                    "Audit is not IN_PROGRESS"
            );
        }

        Equipment equipment =
                equipmentRepository
                        .findByIdAndOrganizationId(
                                request.equipmentId(),
                                audit.getOrganizationId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Equipment not found: "
                                                + request.equipmentId()
                                )
                        );

        StockAuditItem item =
                itemRepository
                        .findByStockAuditIdAndEquipmentId(
                                auditId,
                                request.equipmentId()
                        )
                        .orElseGet(() ->
                                StockAuditItem.builder()
                                        .stockAuditId(auditId)
                                        .equipmentId(
                                                equipment.getId()
                                        )
                                        .expectedWarehouseId(
                                                audit.getWarehouseId()
                                        )
                                        .expectedLocationId(
                                                equipment
                                                        .getWarehouseLocationId()
                                        )
                                        .build()
                        );

        validateResult(
                audit,
                request
        );

        item.setResult(
                request.result()
        );

        item.setActualWarehouseId(
                request.actualWarehouseId()
        );

        item.setActualLocationId(
                request.actualLocationId()
        );

        item.setNote(
                normalize(request.note())
        );

        item.setCheckedBy(
                request.checkedBy()
        );

        item.setCheckedAt(
                LocalDateTime.now()
        );

        itemRepository.save(item);

        return toResponse(audit);
    }

    // =====================================================
    // COMPLETE
    // =====================================================

    @Transactional
    public StockAuditResponse complete(
            Long id,
            CompleteStockAuditRequest request
    ) {

        StockAudit audit =
                getAudit(id);

        if (audit.getStatus()
                != StockAuditStatus.IN_PROGRESS) {

            throw new IllegalStateException(
                    "Only IN_PROGRESS audit can be completed"
            );
        }

        List<StockAuditItem> items =
                itemRepository.findByStockAuditId(
                        audit.getId()
                );

        if (items.isEmpty()) {

            throw new IllegalStateException(
                    "Audit has no items"
            );
        }

        long unchecked =
                itemRepository
                        .countByStockAuditIdAndResultIsNull(
                                audit.getId()
                        );

        if (unchecked > 0) {

            throw new IllegalStateException(
                    "There are "
                            + unchecked
                            + " unchecked equipment items"
            );
        }

        audit.setStatus(
                StockAuditStatus.COMPLETED
        );

        audit.setCompletedBy(
                request == null
                        ? null
                        : request.completedBy()
        );

        audit.setCompletedAt(
                LocalDateTime.now()
        );

        /*
         * Không update Equipment tại đây.
         *
         * FOUND/MISSING/DAMAGED/WRONG_LOCATION
         * chỉ là kết quả kiểm kê.
         */
        return toResponse(
                auditRepository.save(audit)
        );
    }

    // =====================================================
    // CANCEL
    // =====================================================

    @Transactional
    public StockAuditResponse cancel(
            Long id
    ) {

        StockAudit audit =
                getAudit(id);

        if (audit.getStatus()
                == StockAuditStatus.COMPLETED) {

            throw new IllegalStateException(
                    "Completed audit cannot be cancelled"
            );
        }

        if (audit.getStatus()
                == StockAuditStatus.CANCELLED) {

            throw new IllegalStateException(
                    "Audit is already cancelled"
            );
        }

        audit.setStatus(
                StockAuditStatus.CANCELLED
        );

        audit.setCancelledAt(
                LocalDateTime.now()
        );

        return toResponse(
                auditRepository.save(audit)
        );
    }

    // =====================================================
    // VALIDATE RESULT
    // =====================================================

    private void validateResult(
            StockAudit audit,
            RecordStockAuditItemRequest request
    ) {

        if (request.result()
                == StockAuditResult.FOUND) {

            if (request.actualWarehouseId() == null) {

                throw new IllegalArgumentException(
                        "actualWarehouseId is required for FOUND"
                );
            }

            if (!request.actualWarehouseId()
                    .equals(audit.getWarehouseId())) {

                throw new IllegalArgumentException(
                        "FOUND equipment must be in audited warehouse"
                );
            }
        }

        if (request.result()
                == StockAuditResult.WRONG_LOCATION) {

            if (request.actualWarehouseId() == null
                    && request.actualLocationId() == null) {

                throw new IllegalArgumentException(
                        "Actual warehouse/location is required for WRONG_LOCATION"
                );
            }
        }
    }

    // =====================================================
    // RESPONSE
    // =====================================================

    private StockAuditResponse toResponse(
            StockAudit audit
    ) {

        List<StockAuditItem> items =
                itemRepository
                        .findByStockAuditId(
                                audit.getId()
                        );

        List<StockAuditItemResponse> itemResponses =
                items.stream()
                        .map(item ->
                                new StockAuditItemResponse(
                                        item.getId(),
                                        item.getEquipmentId(),

                                        item.getExpectedWarehouseId(),
                                        item.getExpectedLocationId(),

                                        item.getActualWarehouseId(),
                                        item.getActualLocationId(),

                                        item.getResult(),

                                        item.getNote(),

                                        item.getCheckedBy(),
                                        item.getCheckedAt()
                                )
                        )
                        .toList();

        long checkedItems =
                items.stream()
                        .filter(item ->
                                item.getResult() != null
                        )
                        .count();

        long found =
                countResult(
                        items,
                        StockAuditResult.FOUND
                );

        long missing =
                countResult(
                        items,
                        StockAuditResult.MISSING
                );

        long damaged =
                countResult(
                        items,
                        StockAuditResult.DAMAGED
                );

        long wrongLocation =
                countResult(
                        items,
                        StockAuditResult.WRONG_LOCATION
                );

        return new StockAuditResponse(
                audit.getId(),

                audit.getOrganizationId(),
                audit.getBranchId(),
                audit.getWarehouseId(),

                audit.getAuditCode(),
                audit.getStatus(),
                audit.getNote(),

                audit.getCreatedBy(),
                audit.getStartedBy(),
                audit.getCompletedBy(),

                audit.getCreatedAt(),
                audit.getStartedAt(),
                audit.getCompletedAt(),
                audit.getCancelledAt(),

                items.size(),
                checkedItems,

                found,
                missing,
                damaged,
                wrongLocation,

                itemResponses
        );
    }

    private long countResult(
            List<StockAuditItem> items,
            StockAuditResult result
    ) {

        return items.stream()
                .filter(item ->
                        item.getResult() == result
                )
                .count();
    }

    private StockAudit getAudit(
            Long id
    ) {

        return auditRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Stock audit not found: "
                                        + id
                        )
                );
    }

    private String normalize(
            String value
    ) {

        if (value == null
                || value.isBlank()) {

            return null;
        }

        return value.trim();
    }
}
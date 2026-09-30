package com.equipmentrental.inventory.service;

import com.equipmentrental.inventory.dto.request.ConfirmStockInRequest;
import com.equipmentrental.inventory.dto.request.CreateStockInItemRequest;
import com.equipmentrental.inventory.dto.request.CreateStockInRequest;
import com.equipmentrental.inventory.dto.response.StockInItemResponse;
import com.equipmentrental.inventory.dto.response.StockInResponse;
import com.equipmentrental.inventory.entity.Equipment;
import com.equipmentrental.inventory.entity.StockInItem;
import com.equipmentrental.inventory.entity.StockInReceipt;
import com.equipmentrental.inventory.entity.Warehouse;
import com.equipmentrental.inventory.enums.EquipmentStatus;
import com.equipmentrental.inventory.enums.EquipmentTransactionType;
import com.equipmentrental.inventory.enums.StockInStatus;
import com.equipmentrental.inventory.exception.ResourceNotFoundException;
import com.equipmentrental.inventory.repository.EquipmentRepository;
import com.equipmentrental.inventory.repository.StockInItemRepository;
import com.equipmentrental.inventory.repository.StockInReceiptRepository;
import com.equipmentrental.inventory.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockInService {

    private final StockInReceiptRepository receiptRepository;
    private final StockInItemRepository itemRepository;
    private final EquipmentRepository equipmentRepository;
    private final WarehouseRepository warehouseRepository;

    /*
     * Service dùng để ghi lịch sử biến động Equipment.
     */
    private final EquipmentTransactionService transactionService;

    // =====================================================
    // CREATE
    // =====================================================

    @Transactional
    public StockInResponse create(
            CreateStockInRequest request
    ) {

        Warehouse warehouse =
                warehouseRepository
                        .findById(request.warehouseId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Warehouse not found"
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
                request.stockInCode()
                        .trim()
                        .toUpperCase();

        if (receiptRepository
                .existsByOrganizationIdAndStockInCode(
                        request.organizationId(),
                        code
                )) {

            throw new IllegalArgumentException(
                    "Stock-in code already exists"
            );
        }

        /*
         * Kiểm tra toàn bộ Equipment trước
         * khi tạo chứng từ.
         */
        for (CreateStockInItemRequest item
                : request.items()) {

            equipmentRepository
                    .findByIdAndOrganizationId(
                            item.equipmentId(),
                            request.organizationId()
                    )
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Equipment not found: "
                                            + item.equipmentId()
                            )
                    );
        }

        StockInReceipt receipt =
                StockInReceipt.builder()

                        .organizationId(
                                request.organizationId()
                        )

                        .branchId(
                                request.branchId()
                        )

                        .warehouseId(
                                request.warehouseId()
                        )

                        .stockInCode(code)

                        .sourceType(
                                normalize(
                                        request.sourceType()
                                )
                        )

                        .referenceCode(
                                normalize(
                                        request.referenceCode()
                                )
                        )

                        .note(
                                normalize(
                                        request.note()
                                )
                        )

                        .status(
                                StockInStatus.DRAFT
                        )

                        .createdBy(
                                request.createdBy()
                        )

                        .build();

        receipt =
                receiptRepository.save(receipt);

        Long stockInId =
                receipt.getId();

        for (CreateStockInItemRequest requestItem
                : request.items()) {

            StockInItem item =
                    StockInItem.builder()

                            .stockInId(
                                    stockInId
                            )

                            .equipmentId(
                                    requestItem.equipmentId()
                            )

                            .note(
                                    normalize(
                                            requestItem.note()
                                    )
                            )

                            .build();

            itemRepository.save(item);
        }

        return toResponse(receipt);
    }

    // =====================================================
    // GET ALL
    // =====================================================

    @Transactional(readOnly = true)
    public List<StockInResponse> findAll(
            Long organizationId,
            Long branchId,
            Long warehouseId
    ) {

        List<StockInReceipt> receipts;

        if (organizationId != null
                && warehouseId != null) {

            receipts =
                    receiptRepository
                            .findByOrganizationIdAndWarehouseId(
                                    organizationId,
                                    warehouseId
                            );

        } else if (organizationId != null
                && branchId != null) {

            receipts =
                    receiptRepository
                            .findByOrganizationIdAndBranchId(
                                    organizationId,
                                    branchId
                            );

        } else if (organizationId != null) {

            receipts =
                    receiptRepository
                            .findByOrganizationId(
                                    organizationId
                            );

        } else {

            receipts =
                    receiptRepository.findAll();
        }

        return receipts
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =====================================================
    // GET BY ID
    // =====================================================

    @Transactional(readOnly = true)
    public StockInResponse findById(
            Long id
    ) {

        StockInReceipt receipt =
                getReceipt(id);

        return toResponse(receipt);
    }

    // =====================================================
    // CONFIRM
    // =====================================================

    @Transactional
    public StockInResponse confirm(
            Long id,
            ConfirmStockInRequest request
    ) {

        StockInReceipt receipt =
                getReceipt(id);

        if (receipt.getStatus()
                != StockInStatus.DRAFT) {

            throw new IllegalStateException(
                    "Only DRAFT stock-in can be confirmed"
            );
        }

        Warehouse warehouse =
                warehouseRepository
                        .findById(
                                receipt.getWarehouseId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Warehouse not found"
                                )
                        );

        List<StockInItem> items =
                itemRepository
                        .findByStockInId(
                                receipt.getId()
                        );

        if (items.isEmpty()) {

            throw new IllegalStateException(
                    "Stock-in has no items"
            );
        }

        /*
         * Người thực hiện confirm.
         */
        Long confirmedBy =
                request == null
                        ? null
                        : request.confirmedBy();

        for (StockInItem item : items) {

            Equipment equipment =
                    equipmentRepository
                            .findByIdAndOrganizationId(
                                    item.getEquipmentId(),
                                    receipt.getOrganizationId()
                            )
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Equipment not found: "
                                                    + item.getEquipmentId()
                                    )
                            );

            // =================================================
            // LƯU TRẠNG THÁI TRƯỚC KHI THAY ĐỔI
            // =================================================

            Long oldWarehouseId =
                    equipment.getWarehouseId();

            String oldStatus =
                    equipment.getStatus() == null
                            ? null
                            : equipment.getStatus().name();

            /*
             * Chỉ tới CONFIRM mới thực sự cập nhật inventory.
             */
            equipment.setBranchId(
                    warehouse.getBranchId()
            );

            equipment.setWarehouseId(
                    warehouse.getId()
            );

            equipment.setWarehouseLocationId(
                    null
            );

            equipment.setStatus(
                    EquipmentStatus.AVAILABLE
            );

            equipment =
                    equipmentRepository.save(
                            equipment
                    );

            // =================================================
            // XÁC ĐỊNH LOẠI TRANSACTION
            // =================================================

            EquipmentTransactionType transactionType;

            /*
             * RETURN nghĩa là thiết bị được trả về kho.
             *
             * Ví dụ:
             * thiết bị đang CHECKED_OUT
             * -> khách trả
             * -> CHECKIN
             */
            if ("RETURN".equalsIgnoreCase(
                    receipt.getSourceType()
            )) {

                transactionType =
                        EquipmentTransactionType.CHECKIN;

            } else {

                /*
                 * PURCHASE / NEW / OTHER...
                 * được xem là nhập kho thông thường.
                 */
                transactionType =
                        EquipmentTransactionType.STOCK_IN;
            }

            // =================================================
            // GHI EQUIPMENT TRANSACTION
            // =================================================

            transactionService.record(

                    equipment,

                    transactionType,

                    oldWarehouseId,

                    warehouse.getId(),

                    "STOCK_IN",

                    receipt.getId(),

                    receipt.getStockInCode(),

                    oldStatus,

                    equipment.getStatus() == null
                            ? null
                            : equipment.getStatus().name(),

                    confirmedBy,

                    buildTransactionNote(
                            receipt,
                            item,
                            transactionType
                    )
            );
        }

        /*
         * Sau khi toàn bộ Equipment xử lý thành công
         * mới confirm chứng từ.
         */
        receipt.setStatus(
                StockInStatus.CONFIRMED
        );

        receipt.setConfirmedBy(
                confirmedBy
        );

        receipt.setConfirmedAt(
                LocalDateTime.now()
        );

        return toResponse(
                receiptRepository.save(receipt)
        );
    }

    // =====================================================
    // CANCEL
    // =====================================================

    @Transactional
    public StockInResponse cancel(
            Long id
    ) {

        StockInReceipt receipt =
                getReceipt(id);

        if (receipt.getStatus()
                != StockInStatus.DRAFT) {

            throw new IllegalStateException(
                    "Only DRAFT stock-in can be cancelled"
            );
        }

        receipt.setStatus(
                StockInStatus.CANCELLED
        );

        receipt.setCancelledAt(
                LocalDateTime.now()
        );

        /*
         * CANCEL không ghi EquipmentTransaction
         * vì Equipment chưa thực sự thay đổi.
         */

        return toResponse(
                receiptRepository.save(receipt)
        );
    }

    // =====================================================
    // GET RECEIPT
    // =====================================================

    private StockInReceipt getReceipt(
            Long id
    ) {

        return receiptRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Stock-in not found"
                        )
                );
    }

    // =====================================================
    // RESPONSE
    // =====================================================

    private StockInResponse toResponse(
            StockInReceipt receipt
    ) {

        List<StockInItemResponse> items =
                itemRepository
                        .findByStockInId(
                                receipt.getId()
                        )
                        .stream()
                        .map(item ->
                                new StockInItemResponse(

                                        item.getId(),

                                        item.getEquipmentId(),

                                        item.getNote(),

                                        item.getCreatedAt()
                                )
                        )
                        .toList();

        return new StockInResponse(

                receipt.getId(),

                receipt.getOrganizationId(),

                receipt.getBranchId(),

                receipt.getWarehouseId(),

                receipt.getStockInCode(),

                receipt.getSourceType(),

                receipt.getReferenceCode(),

                receipt.getNote(),

                receipt.getStatus(),

                receipt.getCreatedBy(),

                receipt.getConfirmedBy(),

                receipt.getCreatedAt(),

                receipt.getConfirmedAt(),

                receipt.getCancelledAt(),

                items
        );
    }

    // =====================================================
    // TRANSACTION NOTE
    // =====================================================

    private String buildTransactionNote(
            StockInReceipt receipt,
            StockInItem item,
            EquipmentTransactionType type
    ) {

        String prefix =
                type == EquipmentTransactionType.CHECKIN
                        ? "Equipment checked in"
                        : "Equipment stocked in";

        if (item.getNote() != null
                && !item.getNote().isBlank()) {

            return prefix
                    + " - "
                    + item.getNote();
        }

        if (receipt.getNote() != null
                && !receipt.getNote().isBlank()) {

            return prefix
                    + " - "
                    + receipt.getNote();
        }

        return prefix;
    }

    // =====================================================
    // NORMALIZE
    // =====================================================

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
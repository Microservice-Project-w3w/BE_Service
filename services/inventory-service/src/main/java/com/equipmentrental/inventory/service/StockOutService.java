package com.equipmentrental.inventory.service;

import com.equipmentrental.inventory.dto.request.ConfirmStockOutRequest;
import com.equipmentrental.inventory.dto.request.CreateStockOutItemRequest;
import com.equipmentrental.inventory.dto.request.CreateStockOutRequest;
import com.equipmentrental.inventory.dto.response.StockOutItemResponse;
import com.equipmentrental.inventory.dto.response.StockOutResponse;
import com.equipmentrental.inventory.entity.Equipment;
import com.equipmentrental.inventory.entity.StockOutItem;
import com.equipmentrental.inventory.entity.StockOutReceipt;
import com.equipmentrental.inventory.entity.Warehouse;
import com.equipmentrental.inventory.enums.EquipmentStatus;
import com.equipmentrental.inventory.enums.EquipmentTransactionType;
import com.equipmentrental.inventory.enums.StockOutStatus;
import com.equipmentrental.inventory.exception.ResourceNotFoundException;
import com.equipmentrental.inventory.repository.EquipmentRepository;
import com.equipmentrental.inventory.repository.StockOutItemRepository;
import com.equipmentrental.inventory.repository.StockOutReceiptRepository;
import com.equipmentrental.inventory.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockOutService {

    private final StockOutReceiptRepository receiptRepository;
    private final StockOutItemRepository itemRepository;
    private final EquipmentRepository equipmentRepository;
    private final WarehouseRepository warehouseRepository;

    /*
     * Service ghi lịch sử biến động Equipment.
     */
    private final EquipmentTransactionService transactionService;

    // =====================================================
    // CREATE DRAFT
    // =====================================================

    @Transactional
    public StockOutResponse create(
            CreateStockOutRequest request
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
                request.stockOutCode()
                        .trim()
                        .toUpperCase();

        if (receiptRepository
                .existsByOrganizationIdAndStockOutCode(
                        request.organizationId(),
                        code
                )) {

            throw new IllegalArgumentException(
                    "Stock-out code already exists"
            );
        }

        /*
         * Kiểm tra tất cả Equipment trước khi tạo phiếu.
         */
        for (CreateStockOutItemRequest item
                : request.items()) {

            Equipment equipment =
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

            /*
             * Equipment phải thực sự nằm trong kho đang xuất.
             */
            if (equipment.getWarehouseId() == null
                    || !equipment.getWarehouseId()
                    .equals(request.warehouseId())) {

                throw new IllegalArgumentException(
                        "Equipment "
                                + equipment.getId()
                                + " is not in warehouse "
                                + request.warehouseId()
                );
            }

            /*
             * Chỉ Equipment AVAILABLE mới được xuất.
             */
            if (equipment.getStatus()
                    != EquipmentStatus.AVAILABLE) {

                throw new IllegalArgumentException(
                        "Equipment "
                                + equipment.getId()
                                + " is not AVAILABLE"
                );
            }
        }

        StockOutReceipt receipt =
                StockOutReceipt.builder()

                        .organizationId(
                                request.organizationId()
                        )

                        .branchId(
                                request.branchId()
                        )

                        .warehouseId(
                                request.warehouseId()
                        )

                        .stockOutCode(code)

                        .purposeType(
                                normalize(
                                        request.purposeType()
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
                                StockOutStatus.DRAFT
                        )

                        .createdBy(
                                request.createdBy()
                        )

                        .build();

        receipt =
                receiptRepository.save(receipt);

        Long stockOutId =
                receipt.getId();

        for (CreateStockOutItemRequest requestItem
                : request.items()) {

            StockOutItem item =
                    StockOutItem.builder()

                            .stockOutId(
                                    stockOutId
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
    public List<StockOutResponse> findAll(
            Long organizationId,
            Long branchId,
            Long warehouseId
    ) {

        List<StockOutReceipt> receipts;

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
    public StockOutResponse findById(
            Long id
    ) {

        return toResponse(
                getReceipt(id)
        );
    }

    // =====================================================
    // CONFIRM
    // =====================================================

    @Transactional
    public StockOutResponse confirm(
            Long id,
            ConfirmStockOutRequest request
    ) {

        StockOutReceipt receipt =
                getReceipt(id);

        if (receipt.getStatus()
                != StockOutStatus.DRAFT) {

            throw new IllegalStateException(
                    "Only DRAFT stock-out can be confirmed"
            );
        }

        List<StockOutItem> items =
                itemRepository
                        .findByStockOutId(
                                receipt.getId()
                        );

        if (items.isEmpty()) {

            throw new IllegalStateException(
                    "Stock-out has no items"
            );
        }

        /*
         * Kiểm tra toàn bộ Equipment trước.
         *
         * Không update thiết bị đầu tiên rồi mới phát hiện
         * thiết bị thứ hai lỗi.
         */
        for (StockOutItem item : items) {

            Equipment equipment =
                    getEquipment(
                            item.getEquipmentId(),
                            receipt.getOrganizationId()
                    );

            if (equipment.getWarehouseId() == null
                    || !equipment.getWarehouseId()
                    .equals(receipt.getWarehouseId())) {

                throw new IllegalStateException(
                        "Equipment "
                                + equipment.getId()
                                + " is no longer in source warehouse"
                );
            }

            if (equipment.getStatus()
                    != EquipmentStatus.AVAILABLE) {

                throw new IllegalStateException(
                        "Equipment "
                                + equipment.getId()
                                + " is no longer AVAILABLE"
                );
            }
        }

        Long confirmedBy =
                request == null
                        ? null
                        : request.confirmedBy();

        /*
         * Tất cả Equipment hợp lệ.
         * Bắt đầu thực hiện xuất kho.
         */
        for (StockOutItem item : items) {

            Equipment equipment =
                    getEquipment(
                            item.getEquipmentId(),
                            receipt.getOrganizationId()
                    );

            // =================================================
            // SNAPSHOT TRẠNG THÁI TRƯỚC KHI UPDATE
            // =================================================

            Long oldWarehouseId =
                    equipment.getWarehouseId();

            String oldStatus =
                    equipment.getStatus() == null
                            ? null
                            : equipment.getStatus().name();

            // =================================================
            // UPDATE INVENTORY
            // =================================================

            equipment.setWarehouseId(
                    null
            );

            equipment.setWarehouseLocationId(
                    null
            );

            equipment.setStatus(
                    EquipmentStatus.CHECKED_OUT
            );

            equipment =
                    equipmentRepository.save(
                            equipment
                    );

            // =================================================
            // GHI LỊCH SỬ BIẾN ĐỘNG
            // =================================================

            transactionService.record(

                    equipment,

                    EquipmentTransactionType.CHECKOUT,

                    oldWarehouseId,

                    null,

                    "STOCK_OUT",

                    receipt.getId(),

                    receipt.getStockOutCode(),

                    oldStatus,

                    equipment.getStatus() == null
                            ? null
                            : equipment.getStatus().name(),

                    confirmedBy,

                    buildTransactionNote(
                            receipt,
                            item
                    )
            );
        }

        /*
         * Chỉ confirm chứng từ khi toàn bộ Equipment
         * và transaction history đã lưu thành công.
         */
        receipt.setStatus(
                StockOutStatus.CONFIRMED
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
    public StockOutResponse cancel(
            Long id
    ) {

        StockOutReceipt receipt =
                getReceipt(id);

        if (receipt.getStatus()
                != StockOutStatus.DRAFT) {

            throw new IllegalStateException(
                    "Only DRAFT stock-out can be cancelled"
            );
        }

        /*
         * CANCEL không thay Equipment
         * nên cũng không ghi EquipmentTransaction.
         */
        receipt.setStatus(
                StockOutStatus.CANCELLED
        );

        receipt.setCancelledAt(
                LocalDateTime.now()
        );

        return toResponse(
                receiptRepository.save(receipt)
        );
    }

    // =====================================================
    // GET RECEIPT
    // =====================================================

    private StockOutReceipt getReceipt(
            Long id
    ) {

        return receiptRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Stock-out not found"
                        )
                );
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
    // RESPONSE
    // =====================================================

    private StockOutResponse toResponse(
            StockOutReceipt receipt
    ) {

        List<StockOutItemResponse> items =
                itemRepository
                        .findByStockOutId(
                                receipt.getId()
                        )
                        .stream()
                        .map(item ->
                                new StockOutItemResponse(

                                        item.getId(),

                                        item.getEquipmentId(),

                                        item.getNote(),

                                        item.getCreatedAt()
                                )
                        )
                        .toList();

        return new StockOutResponse(

                receipt.getId(),

                receipt.getOrganizationId(),

                receipt.getBranchId(),

                receipt.getWarehouseId(),

                receipt.getStockOutCode(),

                receipt.getPurposeType(),

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
            StockOutReceipt receipt,
            StockOutItem item
    ) {

        String prefix =
                "Equipment checked out";

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
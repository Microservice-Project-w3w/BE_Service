package com.equipmentrental.inventory.service;

import com.equipmentrental.inventory.dto.request.ApproveStockTransferRequest;
import com.equipmentrental.inventory.dto.request.CreateStockTransferItemRequest;
import com.equipmentrental.inventory.dto.request.CreateStockTransferRequest;
import com.equipmentrental.inventory.dto.request.ReceiveStockTransferRequest;
import com.equipmentrental.inventory.dto.response.StockTransferItemResponse;
import com.equipmentrental.inventory.dto.response.StockTransferResponse;
import com.equipmentrental.inventory.entity.Equipment;
import com.equipmentrental.inventory.entity.StockTransfer;
import com.equipmentrental.inventory.entity.StockTransferItem;
import com.equipmentrental.inventory.entity.Warehouse;
import com.equipmentrental.inventory.enums.StockTransferStatus;
import com.equipmentrental.inventory.exception.ResourceNotFoundException;
import com.equipmentrental.inventory.repository.EquipmentRepository;
import com.equipmentrental.inventory.repository.StockTransferItemRepository;
import com.equipmentrental.inventory.repository.StockTransferRepository;
import com.equipmentrental.inventory.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockTransferService {

    private final StockTransferRepository transferRepository;
    private final StockTransferItemRepository itemRepository;
    private final WarehouseRepository warehouseRepository;
    private final EquipmentRepository equipmentRepository;

    @Transactional
    public StockTransferResponse create(
            CreateStockTransferRequest request
    ) {

        if (request.sourceWarehouseId()
                .equals(request.destinationWarehouseId())) {

            throw new IllegalArgumentException(
                    "Source and destination warehouse must be different"
            );
        }

        Warehouse source =
                getWarehouse(
                        request.sourceWarehouseId()
                );

        Warehouse destination =
                getWarehouse(
                        request.destinationWarehouseId()
                );

        validateWarehouse(
                source,
                request.organizationId()
        );

        validateWarehouse(
                destination,
                request.organizationId()
        );

        String code =
                request.transferCode()
                        .trim()
                        .toUpperCase();

        if (transferRepository
                .existsByOrganizationIdAndTransferCode(
                        request.organizationId(),
                        code
                )) {

            throw new IllegalArgumentException(
                    "Transfer code already exists"
            );
        }

        /*
         * Equipment phải đang nằm tại sourceWarehouse.
         */
        for (CreateStockTransferItemRequest item
                : request.items()) {

            Equipment equipment =
                    getEquipment(
                            item.equipmentId(),
                            request.organizationId()
                    );

            if (equipment.getWarehouseId() == null
                    || !equipment.getWarehouseId()
                    .equals(request.sourceWarehouseId())) {

                throw new IllegalArgumentException(
                        "Equipment "
                                + equipment.getId()
                                + " is not in source warehouse"
                );
            }
        }

        StockTransfer transfer =
                StockTransfer.builder()
                        .organizationId(
                                request.organizationId()
                        )
                        .transferCode(code)

                        .fromBranchId(
                                source.getBranchId()
                        )

                        .sourceWarehouseId(
                                source.getId()
                        )

                        .toBranchId(
                                destination.getBranchId()
                        )

                        .destinationWarehouseId(
                                destination.getId()
                        )

                        .note(
                                normalize(
                                        request.note()
                                )
                        )

                        .status(
                                StockTransferStatus.DRAFT
                        )

                        .createdBy(
                                request.createdBy()
                        )

                        .build();

            transfer =
                transferRepository.save(transfer);

        Long transferId =
                transfer.getId();

        for (CreateStockTransferItemRequest requestItem
                : request.items()) {

            StockTransferItem item =
                    StockTransferItem.builder()
                            .transferId(transferId)
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

        return toResponse(transfer);
    }

    @Transactional(readOnly = true)
    public List<StockTransferResponse> findAll(
            Long organizationId,
            Long branchId
    ) {

        List<StockTransfer> result;

        if (organizationId != null
                && branchId != null) {

            List<StockTransfer> fromTransfers =
                    transferRepository
                            .findByOrganizationIdAndFromBranchId(
                                    organizationId,
                                    branchId
                            );

            List<StockTransfer> toTransfers =
                    transferRepository
                            .findByOrganizationIdAndToBranchId(
                                    organizationId,
                                    branchId
                            );

            result =
                    java.util.stream.Stream
                            .concat(
                                    fromTransfers.stream(),
                                    toTransfers.stream()
                            )
                            .distinct()
                            .toList();

        } else if (organizationId != null) {

            result =
                    transferRepository
                            .findByOrganizationId(
                                    organizationId
                            );

        } else {

            result =
                    transferRepository.findAll();
        }

        return result
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StockTransferResponse findById(
            Long id
    ) {

        return toResponse(
                getTransfer(id)
        );
    }

    @Transactional
    public StockTransferResponse approve(
            Long id,
            ApproveStockTransferRequest request
    ) {

        StockTransfer transfer =
                getTransfer(id);

        if (transfer.getStatus()
                != StockTransferStatus.DRAFT) {

            throw new IllegalStateException(
                    "Only DRAFT transfer can be approved"
            );
        }

        transfer.setStatus(
                StockTransferStatus.APPROVED
        );

        transfer.setApprovedBy(
                request == null
                        ? null
                        : request.approvedBy()
        );

        transfer.setApprovedAt(
                LocalDateTime.now()
        );

        return toResponse(
                transferRepository.save(transfer)
        );
    }

    @Transactional
    public StockTransferResponse dispatch(
            Long id
    ) {

        StockTransfer transfer =
                getTransfer(id);

        if (transfer.getStatus()
                != StockTransferStatus.APPROVED) {

            throw new IllegalStateException(
                    "Only APPROVED transfer can be dispatched"
            );
        }

        List<StockTransferItem> items =
                itemRepository
                        .findByTransferId(
                                transfer.getId()
                        );

        if (items.isEmpty()) {

            throw new IllegalStateException(
                    "Transfer has no items"
            );
        }

        /*
         * Kiểm tra lại toàn bộ equipment.
         */
        for (StockTransferItem item : items) {

            Equipment equipment =
                    getEquipment(
                            item.getEquipmentId(),
                            transfer.getOrganizationId()
                    );

            if (equipment.getWarehouseId() == null
                    || !equipment.getWarehouseId()
                    .equals(
                            transfer.getSourceWarehouseId()
                    )) {

                throw new IllegalStateException(
                        "Equipment "
                                + equipment.getId()
                                + " is no longer in source warehouse"
                );
            }
        }

        /*
         * Chưa chuyển destinationWarehouse tại bước này.
         * Chỉ đánh dấu chứng từ đang vận chuyển.
         */
        transfer.setStatus(
                StockTransferStatus.IN_TRANSIT
        );

        transfer.setDispatchedAt(
                LocalDateTime.now()
        );

        return toResponse(
                transferRepository.save(transfer)
        );
    }

    @Transactional
    public StockTransferResponse receive(
            Long id,
            ReceiveStockTransferRequest request
    ) {

        StockTransfer transfer =
                getTransfer(id);

        if (transfer.getStatus()
                != StockTransferStatus.IN_TRANSIT) {

            throw new IllegalStateException(
                    "Only IN_TRANSIT transfer can be received"
            );
        }

        Warehouse destination =
                getWarehouse(
                        transfer.getDestinationWarehouseId()
                );

        List<StockTransferItem> items =
                itemRepository
                        .findByTransferId(
                                transfer.getId()
                        );

        if (items.isEmpty()) {

            throw new IllegalStateException(
                    "Transfer has no items"
            );
        }

        /*
         * RECEIVE mới cập nhật vị trí thật của Equipment.
         */
        for (StockTransferItem item : items) {

            Equipment equipment =
                    getEquipment(
                            item.getEquipmentId(),
                            transfer.getOrganizationId()
                    );

            equipment.setWarehouseId(
                    destination.getId()
            );

            equipment.setBranchId(
                    destination.getBranchId()
            );

            equipment.setWarehouseLocationId(
                    null
            );

            equipmentRepository.save(
                    equipment
            );
        }

        transfer.setStatus(
                StockTransferStatus.RECEIVED
        );

        transfer.setReceivedBy(
                request == null
                        ? null
                        : request.receivedBy()
        );

        transfer.setReceivedAt(
                LocalDateTime.now()
        );

        return toResponse(
                transferRepository.save(transfer)
        );
    }

    @Transactional
    public StockTransferResponse cancel(
            Long id
    ) {

        StockTransfer transfer =
                getTransfer(id);

        if (transfer.getStatus()
                != StockTransferStatus.DRAFT
                && transfer.getStatus()
                != StockTransferStatus.APPROVED) {

            throw new IllegalStateException(
                    "Only DRAFT or APPROVED transfer can be cancelled"
            );
        }

        transfer.setStatus(
                StockTransferStatus.CANCELLED
        );

        transfer.setCancelledAt(
                LocalDateTime.now()
        );

        return toResponse(
                transferRepository.save(transfer)
        );
    }

    private StockTransfer getTransfer(
            Long id
    ) {

        return transferRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Transfer not found"
                        )
                );
    }

    private Warehouse getWarehouse(
            Long id
    ) {

        return warehouseRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Warehouse not found: "
                                        + id
                        )
                );
    }

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

    private void validateWarehouse(
            Warehouse warehouse,
            Long organizationId
    ) {

        if (!warehouse
                .getOrganizationId()
                .equals(organizationId)) {

            throw new IllegalArgumentException(
                    "Warehouse does not belong to organization"
            );
        }

        if (Boolean.FALSE.equals(
                warehouse.getActive()
        )) {

            throw new IllegalArgumentException(
                    "Warehouse is inactive"
            );
        }
    }

    private StockTransferResponse toResponse(
            StockTransfer transfer
    ) {

        List<StockTransferItemResponse> items =
                itemRepository
                        .findByTransferId(
                                transfer.getId()
                        )
                        .stream()
                        .map(item ->
                                new StockTransferItemResponse(
                                        item.getId(),
                                        item.getEquipmentId(),
                                        item.getNote(),
                                        item.getCreatedAt()
                                )
                        )
                        .toList();

        return new StockTransferResponse(
                transfer.getId(),
                transfer.getOrganizationId(),
                transfer.getTransferCode(),

                transfer.getFromBranchId(),
                transfer.getSourceWarehouseId(),

                transfer.getToBranchId(),
                transfer.getDestinationWarehouseId(),

                transfer.getStatus(),
                transfer.getNote(),

                transfer.getCreatedBy(),
                transfer.getApprovedBy(),
                transfer.getReceivedBy(),

                transfer.getCreatedAt(),
                transfer.getApprovedAt(),
                transfer.getDispatchedAt(),
                transfer.getReceivedAt(),
                transfer.getCancelledAt(),

                items
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
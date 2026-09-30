package com.equipmentrental.inventory.repository;

import com.equipmentrental.inventory.entity.StockInReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockInReceiptRepository
        extends JpaRepository<StockInReceipt, Long> {

    List<StockInReceipt>
    findByOrganizationId(
            Long organizationId
    );

    List<StockInReceipt>
    findByOrganizationIdAndBranchId(
            Long organizationId,
            Long branchId
    );

    List<StockInReceipt>
    findByOrganizationIdAndWarehouseId(
            Long organizationId,
            Long warehouseId
    );

    Optional<StockInReceipt>
    findByIdAndOrganizationId(
            Long id,
            Long organizationId
    );

    boolean existsByOrganizationIdAndStockInCode(
            Long organizationId,
            String stockInCode
    );
}
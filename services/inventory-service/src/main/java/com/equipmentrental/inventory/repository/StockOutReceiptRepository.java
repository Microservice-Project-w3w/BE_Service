package com.equipmentrental.inventory.repository;

import com.equipmentrental.inventory.entity.StockOutReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockOutReceiptRepository
        extends JpaRepository<StockOutReceipt, Long> {

    boolean existsByOrganizationIdAndStockOutCode(
            Long organizationId,
            String stockOutCode
    );

    List<StockOutReceipt>
    findByOrganizationId(
            Long organizationId
    );

    List<StockOutReceipt>
    findByOrganizationIdAndBranchId(
            Long organizationId,
            Long branchId
    );

    List<StockOutReceipt>
    findByOrganizationIdAndWarehouseId(
            Long organizationId,
            Long warehouseId
    );
}
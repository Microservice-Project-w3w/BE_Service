package com.equipmentrental.inventory.repository;

import com.equipmentrental.inventory.entity.StockAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockAuditRepository
        extends JpaRepository<StockAudit, Long> {

    boolean existsByOrganizationIdAndAuditCode(
            Long organizationId,
            String auditCode
    );

    List<StockAudit> findByOrganizationId(
            Long organizationId
    );

    List<StockAudit>
    findByOrganizationIdAndBranchId(
            Long organizationId,
            Long branchId
    );

    List<StockAudit>
    findByOrganizationIdAndWarehouseId(
            Long organizationId,
            Long warehouseId
    );
}
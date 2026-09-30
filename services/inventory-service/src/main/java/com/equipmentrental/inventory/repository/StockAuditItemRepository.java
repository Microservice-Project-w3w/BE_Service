package com.equipmentrental.inventory.repository;

import com.equipmentrental.inventory.entity.StockAuditItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockAuditItemRepository
        extends JpaRepository<StockAuditItem, Long> {

    List<StockAuditItem>
    findByStockAuditId(
            Long stockAuditId
    );

    Optional<StockAuditItem>
    findByStockAuditIdAndEquipmentId(
            Long stockAuditId,
            Long equipmentId
    );

    long countByStockAuditIdAndResultIsNull(
            Long stockAuditId
    );
}
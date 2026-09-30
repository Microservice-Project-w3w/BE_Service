package com.equipmentrental.inventory.repository;

import com.equipmentrental.inventory.entity.StockInItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockInItemRepository
        extends JpaRepository<StockInItem, Long> {

    List<StockInItem>
    findByStockInId(
            Long stockInId
    );

    boolean existsByStockInIdAndEquipmentId(
            Long stockInId,
            Long equipmentId
    );
}
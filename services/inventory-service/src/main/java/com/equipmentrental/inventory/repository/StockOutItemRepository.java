package com.equipmentrental.inventory.repository;

import com.equipmentrental.inventory.entity.StockOutItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockOutItemRepository
        extends JpaRepository<StockOutItem, Long> {

    List<StockOutItem>
    findByStockOutId(
            Long stockOutId
    );

    boolean existsByStockOutIdAndEquipmentId(
            Long stockOutId,
            Long equipmentId
    );
}
package com.equipmentrental.inventory.repository;

import com.equipmentrental.inventory.entity.StockTransferItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockTransferItemRepository
        extends JpaRepository<StockTransferItem, Long> {

    List<StockTransferItem> findByTransferId(
            Long transferId
    );
}
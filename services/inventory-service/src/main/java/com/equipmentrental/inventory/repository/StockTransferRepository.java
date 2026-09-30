package com.equipmentrental.inventory.repository;

import com.equipmentrental.inventory.entity.StockTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockTransferRepository
        extends JpaRepository<StockTransfer, Long> {

    boolean existsByOrganizationIdAndTransferCode(
            Long organizationId,
            String transferCode
    );

    Optional<StockTransfer> findByOrganizationIdAndTransferCode(
            Long organizationId,
            String transferCode
    );

    List<StockTransfer> findByOrganizationId(
            Long organizationId
    );

    List<StockTransfer> findByOrganizationIdAndFromBranchId(
            Long organizationId,
            Long fromBranchId
    );

    List<StockTransfer> findByOrganizationIdAndToBranchId(
            Long organizationId,
            Long toBranchId
    );

    List<StockTransfer> findBySourceWarehouseId(
            Long sourceWarehouseId
    );

    List<StockTransfer> findByDestinationWarehouseId(
            Long destinationWarehouseId
    );
}